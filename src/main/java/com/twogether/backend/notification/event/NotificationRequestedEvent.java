package com.twogether.backend.notification.event;

import com.twogether.backend.notification.domain.NotificationType;

import java.util.Map;

/**
 * 알림 발송 요청 이벤트.
 *
 * 어떤 도메인이든 이 이벤트만 발행하면 알림이 저장·발송된다(결합도 0).
 * 예: gathering 이 신청/수락/확정 시점에 발행 → NotificationEventListener 가 수신.
 * (gathering 측 발행 연계는 gathering 담당과 협의 필요 — 이 이슈는 수신/발송까지 제공.)
 */
public record NotificationRequestedEvent(
        Long recipientUserId,
        NotificationType type,
        String title,
        String content,
        Map<String, Object> meta
) {
}
