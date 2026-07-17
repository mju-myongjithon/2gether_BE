package com.twogether.backend.chat.service;

import com.twogether.backend.chat.domain.ChatRoom;
import com.twogether.backend.chat.domain.Message;
import com.twogether.backend.chat.domain.MessageAttachment;
import com.twogether.backend.chat.domain.MessageType;
import com.twogether.backend.chat.dto.request.ChatMessageSendRequest;
import com.twogether.backend.chat.dto.request.ImageAttachmentRequest;
import com.twogether.backend.chat.dto.request.ImageMessageSendRequest;
import com.twogether.backend.chat.dto.response.ChatMessagePageResponse;
import com.twogether.backend.chat.dto.response.ChatMessageResponse;
import com.twogether.backend.chat.dto.response.MessageAttachmentResponse;
import com.twogether.backend.chat.event.MessageCreatedEvent;
import com.twogether.backend.chat.repository.ChatRoomMemberRepository;
import com.twogether.backend.chat.repository.ChatRoomRepository;
import com.twogether.backend.chat.repository.MessageAttachmentRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 메시지 전송/조회 서비스.
 *
 * - 텍스트 전송({@link #send}): REST 폴백과 STOMP 공통 진입점.
 * - 이미지 전송({@link #sendImage}): 첨부는 message_attachment 로 분리 저장(content 겸용 금지).
 * - 조회({@link #getMessages}): (chat_room_id, id) 키셋 커서 페이징(최신 → 과거), 첨부 배치 로딩.
 *
 * 공통: 저장 → last_message 캐시 갱신 → /sub 브로드캐스트 → MessageCreatedEvent 발행.
 * client_message_id 로 방 내 중복 전송을 멱등 처리한다.
 */
@Service
@Transactional(readOnly = true)
public class ChatMessageService {

    private static final String BROADCAST_DESTINATION_PREFIX = "/sub/chat/rooms/";
    private static final String IMAGE_PREVIEW = "(이미지)";
    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final MessageRepository messageRepository;
    private final MessageAttachmentRepository messageAttachmentRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ApplicationEventPublisher eventPublisher;

    public ChatMessageService(
            ChatRoomRepository chatRoomRepository,
            ChatRoomMemberRepository chatRoomMemberRepository,
            MessageRepository messageRepository,
            MessageAttachmentRepository messageAttachmentRepository,
            UserRepository userRepository,
            SimpMessagingTemplate messagingTemplate,
            ApplicationEventPublisher eventPublisher
    ) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatRoomMemberRepository = chatRoomMemberRepository;
        this.messageRepository = messageRepository;
        this.messageAttachmentRepository = messageAttachmentRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 텍스트 메시지를 저장하고 구독자에게 브로드캐스트한다. 저장 후 MessageCreatedEvent 를 발행한다.
     * client_message_id 로 재전송을 멱등 처리한다.
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
        validateTextType(request.type());

        if (request.clientMessageId() != null) {
            Optional<Message> duplicated = messageRepository
                    .findByChatRoomIdAndClientMessageId(roomId, request.clientMessageId());
            if (duplicated.isPresent()) {
                return ChatMessageResponse.from(duplicated.get());
            }
        }

        Message message = messageRepository.save(
                Message.text(room, sender, request.content(), request.clientMessageId())
        );
        room.updateLastMessage(message.getId(), message.getCreatedAt());

        ChatMessageResponse response = ChatMessageResponse.from(message);
        broadcastAndPublish(roomId, message, sender.getId(), request.content(), response);

        return response;
    }

    /**
     * 이미지 메시지를 저장한다. 첨부는 message_attachment 로 분리 저장하고,
     * 응답/브로드캐스트에 첨부 목록을 포함한다. client_message_id 로 멱등 처리.
     */
    @Transactional
    public ChatMessageResponse sendImage(
            String authUserId,
            Long roomId,
            ImageMessageSendRequest request
    ) {
        User sender = findUser(authUserId);
        ChatRoom room = findRoom(roomId);
        verifyParticipant(roomId, sender.getId());

        if (request.clientMessageId() != null) {
            Optional<Message> duplicated = messageRepository
                    .findByChatRoomIdAndClientMessageId(roomId, request.clientMessageId());
            if (duplicated.isPresent()) {
                return ChatMessageResponse.from(duplicated.get(), loadAttachments(duplicated.get().getId()));
            }
        }

        Message message = messageRepository.save(
                Message.image(room, sender, request.caption(), request.clientMessageId())
        );

        List<MessageAttachment> attachments = new ArrayList<>();
        List<ImageAttachmentRequest> items = request.attachments();
        for (int i = 0; i < items.size(); i++) {
            ImageAttachmentRequest item = items.get(i);
            attachments.add(new MessageAttachment(
                    message,
                    item.fileUrl(),
                    item.thumbnailUrl(),
                    item.contentType(),
                    item.size(),
                    item.width(),
                    item.height(),
                    (short) i
            ));
        }
        List<MessageAttachmentResponse> attachmentResponses =
                messageAttachmentRepository.saveAll(attachments).stream()
                        .map(MessageAttachmentResponse::from)
                        .toList();

        room.updateLastMessage(message.getId(), message.getCreatedAt());

        ChatMessageResponse response = ChatMessageResponse.from(message, attachmentResponses);
        broadcastAndPublish(roomId, message, sender.getId(), IMAGE_PREVIEW, response);

        return response;
    }

    /**
     * 채팅방 메시지 이력을 커서 페이징으로 조회한다(최신 → 과거). 이미지 메시지의 첨부는 배치 로딩한다.
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
        Pageable pageable = PageRequest.of(0, limit + 1);

        List<Message> rows = cursor == null
                ? messageRepository.findByChatRoomIdOrderByIdDesc(roomId, pageable)
                : messageRepository.findByChatRoomIdAndIdLessThanOrderByIdDesc(roomId, cursor, pageable);

        boolean hasNext = rows.size() > limit;
        List<Message> pageRows = hasNext ? rows.subList(0, limit) : rows;

        Map<Long, List<MessageAttachmentResponse>> attachmentsByMessage =
                loadAttachmentsFor(pageRows);

        List<ChatMessageResponse> content = pageRows.stream()
                .map(message -> ChatMessageResponse.from(
                        message,
                        attachmentsByMessage.getOrDefault(message.getId(), List.of())
                ))
                .toList();

        Long nextCursor = hasNext && !content.isEmpty()
                ? content.get(content.size() - 1).messageId()
                : null;

        return ChatMessagePageResponse.of(content, nextCursor, hasNext, limit);
    }

    private Map<Long, List<MessageAttachmentResponse>> loadAttachmentsFor(
            List<Message> messages
    ) {
        List<Long> imageMessageIds = messages.stream()
                .filter(message -> message.getType() == MessageType.IMAGE)
                .map(Message::getId)
                .toList();

        if (imageMessageIds.isEmpty()) {
            return Map.of();
        }

        return messageAttachmentRepository
                .findAllByMessageIdInOrderByMessageIdAscSortOrderAsc(imageMessageIds).stream()
                .collect(Collectors.groupingBy(
                        attachment -> attachment.getMessage().getId(),
                        Collectors.mapping(MessageAttachmentResponse::from, Collectors.toList())
                ));
    }

    private List<MessageAttachmentResponse> loadAttachments(
            Long messageId
    ) {
        return messageAttachmentRepository.findAllByMessageIdOrderBySortOrderAsc(messageId).stream()
                .map(MessageAttachmentResponse::from)
                .toList();
    }

    private void broadcastAndPublish(
            Long roomId,
            Message message,
            Long senderId,
            String preview,
            ChatMessageResponse response
    ) {
        messagingTemplate.convertAndSend(BROADCAST_DESTINATION_PREFIX + roomId, response);
        eventPublisher.publishEvent(
                new MessageCreatedEvent(
                        roomId,
                        message.getId(),
                        senderId,
                        message.getType(),
                        preview,
                        message.getCreatedAt()
                )
        );
    }

    private void validateTextType(
            MessageType type
    ) {
        // 이 진입점은 텍스트 전용. 이미지는 sendImage, SYSTEM/CARD 는 서버가 생성.
        if (type != MessageType.TEXT) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
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
