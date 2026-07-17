package com.twogether.backend.chat.service;

import com.twogether.backend.chat.domain.ChatRoom;
import com.twogether.backend.chat.domain.Message;
import com.twogether.backend.chat.domain.MessageType;
import com.twogether.backend.chat.dto.request.ChatMessageSendRequest;
import com.twogether.backend.chat.dto.response.ChatMessagePageResponse;
import com.twogether.backend.chat.dto.response.ChatMessageResponse;
import com.twogether.backend.chat.event.MessageCreatedEvent;
import com.twogether.backend.chat.repository.ChatRoomMemberRepository;
import com.twogether.backend.chat.repository.ChatRoomRepository;
import com.twogether.backend.chat.repository.MessageRepository;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.user.domain.User;
import com.twogether.backend.user.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 메시지 전송/조회 서비스.
 *
 * - 전송: REST 폴백과 STOMP 양쪽에서 공통 진입점({@link #send})을 사용.
 *   저장 → last_message 캐시 갱신 → 구독자 브로드캐스트 → MessageCreatedEvent 발행.
 * - 조회: (chat_room_id, id) 키셋 커서 페이징(최신 → 과거).
 */
@Service
@Transactional(readOnly = true)
public class ChatMessageService {

    private static final String BROADCAST_DESTINATION_PREFIX = "/sub/chat/rooms/";
    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ApplicationEventPublisher eventPublisher;

    public ChatMessageService(
            ChatRoomRepository chatRoomRepository,
            ChatRoomMemberRepository chatRoomMemberRepository,
            MessageRepository messageRepository,
            UserRepository userRepository,
            SimpMessagingTemplate messagingTemplate,
            ApplicationEventPublisher eventPublisher
    ) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatRoomMemberRepository = chatRoomMemberRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 메시지를 저장하고 구독자에게 브로드캐스트한다. 저장 후 MessageCreatedEvent 를 발행한다.
     * client_message_id 로 방 내 중복 전송을 멱등 처리한다.
     */
    @Transactional
    public ChatMessageResponse send(
            String authUserId,
            Long roomId,
            ChatMessageSendRequest request
    ) {
        User sender = findUser(authUserId);
        ChatRoom room = findRoom(roomId);
        verifyParticipant(roomId, sender.getId());
        validateSendableType(request.type());

        // 멱등 처리: 이미 저장된 재전송이면 브로드캐스트 없이 기존 메시지를 반환
        if (request.clientMessageId() != null) {
            Optional<Message> duplicated = messageRepository
                    .findByChatRoomIdAndClientMessageId(roomId, request.clientMessageId());
            if (duplicated.isPresent()) {
                return ChatMessageResponse.from(duplicated.get());
            }
        }

        Message message = messageRepository.save(
                buildMessage(room, sender, request)
        );
        room.updateLastMessage(message.getId(), message.getCreatedAt());

        ChatMessageResponse response = ChatMessageResponse.from(message);

        messagingTemplate.convertAndSend(
                BROADCAST_DESTINATION_PREFIX + roomId,
                response
        );
        eventPublisher.publishEvent(
                new MessageCreatedEvent(
                        roomId,
                        message.getId(),
                        sender.getId(),
                        message.getType(),
                        preview(message),
                        message.getCreatedAt()
                )
        );

        return response;
    }

    /**
     * 채팅방 메시지 이력을 커서 페이징으로 조회한다(최신 → 과거).
     *
     * @param cursor 이전 응답의 nextCursor(messageId). 첫 조회는 null.
     */
    public ChatMessagePageResponse getMessages(
            String authUserId,
            Long roomId,
            Long cursor,
            int size
    ) {
        User me = findUser(authUserId);
        findRoom(roomId);
        verifyParticipant(roomId, me.getId());

        int limit = normalizeSize(size);
        // hasNext 판별을 위해 limit+1 개를 조회
        Pageable pageable = PageRequest.of(0, limit + 1);

        List<Message> rows = cursor == null
                ? messageRepository.findByChatRoomIdOrderByIdDesc(roomId, pageable)
                : messageRepository.findByChatRoomIdAndIdLessThanOrderByIdDesc(roomId, cursor, pageable);

        boolean hasNext = rows.size() > limit;
        List<Message> pageRows = hasNext ? rows.subList(0, limit) : rows;

        List<ChatMessageResponse> content = pageRows.stream()
                .map(ChatMessageResponse::from)
                .toList();

        Long nextCursor = hasNext && !content.isEmpty()
                ? content.get(content.size() - 1).messageId()
                : null;

        return ChatMessagePageResponse.of(content, nextCursor, hasNext, limit);
    }

    private Message buildMessage(
            ChatRoom room,
            User sender,
            ChatMessageSendRequest request
    ) {
        return request.type() == MessageType.IMAGE
                ? Message.image(room, sender, request.content(), request.clientMessageId())
                : Message.text(room, sender, request.content(), request.clientMessageId());
    }

    private void validateSendableType(
            MessageType type
    ) {
        // 사용자가 직접 보낼 수 있는 타입은 TEXT/IMAGE 뿐. SYSTEM/CARD 는 서버가 생성한다.
        if (type != MessageType.TEXT && type != MessageType.IMAGE) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }

    private String preview(
            Message message
    ) {
        return message.getType() == MessageType.IMAGE
                ? "(이미지)"
                : message.getContent();
    }

    private int normalizeSize(
            int size
    ) {
        if (size <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    private void verifyParticipant(
            Long roomId,
            Long userId
    ) {
        boolean participating = chatRoomMemberRepository
                .existsByChatRoomIdAndUserIdAndLeftAtIsNull(roomId, userId);
        if (!participating) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private ChatRoom findRoom(
            Long roomId
    ) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));
    }

    private User findUser(
            String authUserId
    ) {
        return userRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
