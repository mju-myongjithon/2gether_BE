package com.twogether.backend.chat.event;

import com.twogether.backend.chat.domain.MessageType;

import java.time.OffsetDateTime;

/**
 * 메시지 저장 직후 발행되는 도메인 이벤트.
 *
 * 채팅 코어(메시지 저장)와 알림을 느슨하게 연결하기 위한 훅.
 * 알림 도메인(N3)이 이 이벤트를 {@code @EventListener} 로 구독해
 * 인앱/텔레그램 발송을 수행한다. 채팅 쪽에는 리스너가 없어 알림 미구현 상태에서도 무해하다.
 */
public record MessageCreatedEvent(
        Long roomId,
        Long messageId,
        Long senderId,
        MessageType type,
        String preview,
        OffsetDateTime createdAt
) {
}
