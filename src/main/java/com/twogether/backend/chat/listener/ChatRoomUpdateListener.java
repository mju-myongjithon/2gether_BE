package com.twogether.backend.chat.listener;

import com.twogether.backend.chat.domain.ChatRoomMember;
import com.twogether.backend.chat.dto.response.ChatRoomUpdateResponse;
import com.twogether.backend.chat.event.MessageCreatedEvent;
import com.twogether.backend.chat.repository.ChatRoomMemberRepository;
import com.twogether.backend.chat.service.ChatUpdateStreamService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 새 메시지 이벤트 → 방 참여자 전원의 채팅방 목록 SSE 갱신.
 *
 * STOMP 는 현재 열어둔 방 토픽만 구독하므로, 다른 방에 온 메시지는
 * 이 리스너가 사용자별 SSE 채널(room-update)로 알려 목록을 실시간 갱신한다.
 * 발신자에게도 보내 다른 탭/기기의 목록을 동기화한다.
 */
@Component
public class ChatRoomUpdateListener {

    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatUpdateStreamService chatUpdateStreamService;

    public ChatRoomUpdateListener(
            ChatRoomMemberRepository chatRoomMemberRepository,
            ChatUpdateStreamService chatUpdateStreamService
    ) {
        this.chatRoomMemberRepository = chatRoomMemberRepository;
        this.chatUpdateStreamService = chatUpdateStreamService;
    }

    @Async
    @Transactional(readOnly = true)
    @EventListener
    public void onMessageCreated(MessageCreatedEvent event) {
        ChatRoomUpdateResponse payload = new ChatRoomUpdateResponse(
                event.roomId(),
                event.messageId(),
                event.senderId(),
                event.type(),
                event.preview(),
                event.createdAt()
        );

        List<ChatRoomMember> members =
                chatRoomMemberRepository.findAllByChatRoomIdAndLeftAtIsNull(event.roomId());

        for (ChatRoomMember member : members) {
            chatUpdateStreamService.sendRoomUpdate(member.getUser().getId(), payload);
        }
    }
}
