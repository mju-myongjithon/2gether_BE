package com.twogether.backend.chat.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채팅방 유형")
public enum ChatRoomType {

    @Schema(description = "모임 확정 시 생성되는 그룹 채팅")
    GROUP,

    @Schema(description = "퀵 코드 기반 1:1 빠른 연결 채팅")
    QUICK_CONNECT
}
