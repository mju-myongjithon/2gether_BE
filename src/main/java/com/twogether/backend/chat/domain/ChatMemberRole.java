package com.twogether.backend.chat.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채팅방 멤버 역할 (공지/강퇴 등 권한 구분)")
public enum ChatMemberRole {

    @Schema(description = "방장 (공지 등록 등 관리 권한)")
    OWNER,

    @Schema(description = "일반 참여자")
    MEMBER
}
