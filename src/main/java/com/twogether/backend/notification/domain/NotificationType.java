package com.twogether.backend.notification.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "알림 유형")
public enum NotificationType {

    @Schema(description = "모임 신청 접수(방장에게)")
    GATHERING_APPLICATION,

    @Schema(description = "신청 수락(신청자에게)")
    APPLICATION_ACCEPTED,

    @Schema(description = "신청 거절(신청자에게)")
    APPLICATION_REJECTED,

    @Schema(description = "모임 확정")
    GATHERING_CONFIRMED,

    @Schema(description = "모임 취소")
    GATHERING_CANCELED,

    @Schema(description = "채팅방 공지 등록")
    CHAT_NOTICE,

    @Schema(description = "새 채팅 메시지")
    CHAT_MESSAGE,

    @Schema(description = "시스템 알림")
    SYSTEM
}
