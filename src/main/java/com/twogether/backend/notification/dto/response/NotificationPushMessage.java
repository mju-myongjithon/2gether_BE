package com.twogether.backend.notification.dto.response;

import com.twogether.backend.notification.domain.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * 실시간 인앱 푸시 페이로드. 구독 경로: /sub/users/{userId}/notifications
 * kind = NOTIFICATION(이벤트성 알림함) | CHAT_MESSAGE(새 채팅 메시지, 저장 안 함)
 */
@Schema(description = "인앱 실시간 알림 푸시 메시지")
public record NotificationPushMessage(

        @Schema(description = "종류", example = "NOTIFICATION", allowableValues = {"NOTIFICATION", "CHAT_MESSAGE"})
        String kind,

        @Schema(description = "알림 유형(이벤트성일 때)", example = "APPLICATION_ACCEPTED")
        NotificationType type,

        @Schema(description = "제목", example = "신청이 수락되었습니다")
        String title,

        @Schema(description = "내용/미리보기", example = "새 메시지 3개")
        String content,

        @Schema(description = "이동 대상 메타(JSON)")
        Map<String, Object> meta,

        @Schema(description = "관련 채팅방 ID(새 메시지일 때)", example = "10")
        Long roomId,

        @Schema(description = "발생 일시", example = "2026-07-17T16:00:00+09:00")
        OffsetDateTime timestamp

) {

    public static NotificationPushMessage notification(
            NotificationType type, String title, String content, Map<String, Object> meta, OffsetDateTime timestamp
    ) {
        return new NotificationPushMessage("NOTIFICATION", type, title, content, meta, null, timestamp);
    }

    public static NotificationPushMessage chatMessage(
            Long roomId, String content, OffsetDateTime timestamp
    ) {
        return new NotificationPushMessage("CHAT_MESSAGE", null, null, content, null, roomId, timestamp);
    }
}
