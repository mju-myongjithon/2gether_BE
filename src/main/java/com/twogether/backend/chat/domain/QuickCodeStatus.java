package com.twogether.backend.chat.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "안심 커넥트 코드 상태")
public enum QuickCodeStatus {

    @Schema(description = "사용 가능(활성)")
    ACTIVE,

    @Schema(description = "만료됨")
    EXPIRED,

    @Schema(description = "사용 완료")
    USED
}
