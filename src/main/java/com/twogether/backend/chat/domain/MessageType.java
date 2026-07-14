package com.twogether.backend.chat.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "메시지 유형")
public enum MessageType {

    @Schema(description = "텍스트 메시지")
    TEXT,

    @Schema(description = "이미지 메시지")
    IMAGE,

    @Schema(description = "시스템 메시지 (입장/퇴장 등)")
    SYSTEM
}
