package com.twogether.backend.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "알림 안읽음 수 응답")
public record NotificationUnreadCountResponse(

        @Schema(description = "안읽은 알림 수", example = "3")
        long unreadCount

) {

    public static NotificationUnreadCountResponse of(long unreadCount) {
        return new NotificationUnreadCountResponse(unreadCount);
    }
}
