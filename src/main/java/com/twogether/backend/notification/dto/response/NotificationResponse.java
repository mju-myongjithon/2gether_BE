package com.twogether.backend.notification.dto.response;

import com.twogether.backend.notification.domain.Notification;
import com.twogether.backend.notification.domain.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.Map;

@Schema(description = "알림 응답")
public record NotificationResponse(

        @Schema(description = "알림 ID", example = "12")
        Long notificationId,

        @Schema(description = "알림 유형", example = "APPLICATION_ACCEPTED")
        NotificationType type,

        @Schema(description = "제목", example = "신청이 수락되었습니다")
        String title,

        @Schema(description = "내용", example = "'인문X자연 해커톤 팀' 모임 신청이 수락되었습니다.")
        String content,

        @Schema(description = "이동 대상 메타(JSON). 예: {\"gatheringId\":1} 또는 {\"roomId\":10,\"messageId\":105}")
        Map<String, Object> meta,

        @Schema(description = "읽음 여부", example = "false")
        boolean read,

        @Schema(description = "생성 일시", example = "2026-07-14T10:00:00+09:00")
        OffsetDateTime createdAt

) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getContent(),
                notification.getMeta(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
