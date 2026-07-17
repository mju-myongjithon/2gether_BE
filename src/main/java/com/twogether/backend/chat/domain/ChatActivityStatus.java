package com.twogether.backend.chat.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "최근 7일 메시지 수 기준 채팅방 활동 상태")
public enum ChatActivityStatus {
    INACTIVE, LOW, MEDIUM, HIGH
}
