package com.twogether.backend.notification.listener;

import com.twogether.backend.chat.domain.ChatRoomMember;
import com.twogether.backend.chat.domain.MessageType;
import com.twogether.backend.chat.event.MessageCreatedEvent;
import com.twogether.backend.chat.repository.ChatRoomMemberRepository;
import com.twogether.backend.notification.event.NotificationRequestedEvent;
import com.twogether.backend.notification.service.NotificationDispatchService;
import com.twogether.backend.notification.support.MessageNotificationDebouncer;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 도메인 이벤트 → 알림 발송 리스너(비동기).
 *
 * 채팅 코어(메시지 저장)와 분리된 리스너라 결합도 0. 텔레그램/발송 장애가 채팅 흐름을 막지 않는다.
 */
@Component
public class NotificationEventListener {

    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final MessageNotificationDebouncer debouncer;
    private final NotificationDispatchService dispatchService;

    public NotificationEventListener(
            ChatRoomMemberRepository chatRoomMemberRepository,
            MessageNotificationDebouncer debouncer,
            NotificationDispatchService dispatchService
    ) {
        this.chatRoomMemberRepository = chatRoomMemberRepository;
        this.debouncer = debouncer;
        this.dispatchService = dispatchService;
    }

    /**
     * 새 채팅 메시지 → 참여자에게 알림(발신자 제외, 뮤트 존중, 디바운스).
     * 실사용 메시지(TEXT/IMAGE)만 대상. SYSTEM/CARD 는 제외.
     */
    @Async
    @Transactional(readOnly = true)
    @EventListener
    public void onMessageCreated(MessageCreatedEvent event) {
        if (event.type() != MessageType.TEXT && event.type() != MessageType.IMAGE) {
            return;
        }

        List<ChatRoomMember> members =
                chatRoomMemberRepository.findAllByChatRoomIdAndLeftAtIsNull(event.roomId());

        for (ChatRoomMember member : members) {
            Long recipientUserId = member.getUser().getId();
            if (recipientUserId.equals(event.senderId())) {
                continue; // 발신자 제외
            }
            if (!member.isNotificationEnabled()) {
                continue; // 방 뮤트 존중
            }
            debouncer.record(event.roomId(), recipientUserId, event.preview());
        }
    }

    /**
     * 알림 요청 이벤트(신청/수락/확정/공지 등) → 저장 + 인앱 + 텔레그램.
     */
    @Async
    @EventListener
    public void onNotificationRequested(NotificationRequestedEvent event) {
        dispatchService.notifyEvent(
                event.recipientUserId(),
                event.type(),
                event.title(),
                event.content(),
                event.meta()
        );
    }
}
