package com.twogether.backend.chat.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "메시지 유형")
public enum MessageType {

    @Schema(description = "텍스트 메시지")
    TEXT,

    @Schema(description = "이미지 메시지 (message_attachment 참조)")
    IMAGE,

    @Schema(description = "시스템 메시지 (입장/퇴장/확정 등, sender=null, 상세는 meta)")
    SYSTEM,

    @Schema(description = "카드 메시지 (AI 추천/공유 카드, 상세는 meta)")
    CARD
}
