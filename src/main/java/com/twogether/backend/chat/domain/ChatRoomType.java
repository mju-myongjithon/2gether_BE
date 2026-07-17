package com.twogether.backend.chat.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채팅방 유형")
public enum ChatRoomType {

    @Schema(description = "모임 확정 시 생성되는 그룹 채팅")
    GROUP,

    @Schema(description = "1:1 다이렉트 채팅")
    DIRECT,

    @Schema(description = "퀵 코드 기반 빠른 연결(안심 커넥트) 채팅")
    QUICK_CONNECT
}
