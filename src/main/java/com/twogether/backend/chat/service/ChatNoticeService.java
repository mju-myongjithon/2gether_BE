package com.twogether.backend.chat.service;

import com.twogether.backend.chat.domain.ChatMemberRole;
import com.twogether.backend.chat.domain.ChatNotice;
import com.twogether.backend.chat.domain.ChatRoom;
import com.twogether.backend.chat.domain.ChatRoomMember;
import com.twogether.backend.chat.domain.Message;
import com.twogether.backend.chat.dto.request.ChatNoticeCreateRequest;
import com.twogether.backend.chat.dto.response.ChatMessageResponse;
import com.twogether.backend.chat.dto.response.ChatNoticeResponse;
import com.twogether.backend.chat.repository.ChatNoticeRepository;
import com.twogether.backend.chat.repository.ChatRoomMemberRepository;
import com.twogether.backend.chat.repository.ChatRoomRepository;
import com.twogether.backend.chat.repository.MessageRepository;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.user.domain.User;
import com.twogether.backend.user.repository.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 채팅방 공지 서비스.
 *
 * - 방당 활성 공지는 1건. 신규 등록 시 기존 활성 공지를 해제하고 새 공지를 활성화한다.
 *   (활성 공지 유일성의 DB 강제는 부분 유니크 WHERE is_active 로 보장해야 하며,
 *    ddl-auto=update 로는 생성되지 않으므로 마이그레이션에서 인덱스를 추가한다. 여기서는 앱 레벨로 보장.)
 * - 등록/해제 권한은 chat_room_member.role = OWNER.
 * - 공지 등록 시 SYSTEM 메시지를 발행해 채팅 흐름에도 표시하고 구독자에게 브로드캐스트한다.
 */
@Service
@Transactional(readOnly = true)
public class ChatNoticeService {

    private static final String BROADCAST_DESTINATION_PREFIX = "/sub/chat/rooms/";

    private final ChatNoticeRepository chatNoticeRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatNoticeService(
            ChatNoticeRepository chatNoticeRepository,
            ChatRoomRepository chatRoomRepository,
            ChatRoomMemberRepository chatRoomMemberRepository,
            MessageRepository messageRepository,
            UserRepository userRepository,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.chatNoticeRepository = chatNoticeRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.chatRoomMemberRepository = chatRoomMemberRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * 공지를 등록한다(OWNER 전용). 기존 활성 공지를 해제하고 새 공지를 활성화한 뒤,
     * SYSTEM 메시지를 발행/브로드캐스트한다.
     */
    @Transactional
    public ChatNoticeResponse register(
            String authUserId,
            Long roomId,
            ChatNoticeCreateRequest request
    ) {
        User owner = findUser(authUserId);
        ChatRoom room = findRoom(roomId);
        requireOwner(roomId, owner.getId());

        chatNoticeRepository.findByChatRoomIdAndIsActiveTrue(roomId)
                .ifPresent(ChatNotice::deactivate);

        ChatNotice notice = chatNoticeRepository.save(
                new ChatNotice(room, request.messageId(), request.content(), owner)
        );

        publishNoticeSystemMessage(room, notice);

        return ChatNoticeResponse.from(notice);
    }

    /**
     * 현재 활성 공지를 해제한다(OWNER 전용).
     */
    @Transactional
    public void deactivateActive(
            String authUserId,
            Long roomId
    ) {
        User owner = findUser(authUserId);
        findRoom(roomId);
        requireOwner(roomId, owner.getId());

        ChatNotice active = chatNoticeRepository.findByChatRoomIdAndIsActiveTrue(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_NOTICE_NOT_FOUND));

        active.deactivate();
    }

    /**
     * 현재 활성 공지를 조회한다(참여자). 없으면 null.
     */
    public ChatNoticeResponse getActive(
            String authUserId,
            Long roomId
    ) {
        User me = findUser(authUserId);
        findRoom(roomId);
        requireParticipant(roomId, me.getId());

        return chatNoticeRepository.findByChatRoomIdAndIsActiveTrue(roomId)
                .map(ChatNoticeResponse::from)
                .orElse(null);
    }

    private void publishNoticeSystemMessage(
            ChatRoom room,
            ChatNotice notice
    ) {
        Message systemMessage = messageRepository.save(
                Message.system(
                        room,
                        notice.getContent(),
                        Map.of(
                                "systemType", "NOTICE_REGISTERED",
                                "noticeId", notice.getId()
                        )
                )
        );
        room.updateLastMessage(systemMessage.getId(), systemMessage.getCreatedAt());

        messagingTemplate.convertAndSend(
                BROADCAST_DESTINATION_PREFIX + room.getId(),
                ChatMessageResponse.from(systemMessage)
        );
    }

    private ChatRoomMember requireOwner(
            Long roomId,
            Long userId
    ) {
        ChatRoomMember membership = chatRoomMemberRepository
                .findByChatRoomIdAndUserId(roomId, userId)
                .filter(ChatRoomMember::isParticipating)
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN));

        if (membership.getRole() != ChatMemberRole.OWNER) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return membership;
    }

    private void requireParticipant(
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
