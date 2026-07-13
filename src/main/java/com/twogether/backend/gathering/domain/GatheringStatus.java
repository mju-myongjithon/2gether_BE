package com.twogether.backend.gathering.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "모임 상태")
public enum GatheringStatus {

    @Schema(description = "모집 중")
    RECRUITING,

    @Schema(description = "모집 확정")
    CONFIRMED,

    @Schema(description = "모임 종료")
    COMPLETED,

    @Schema(description = "모임 취소")
    CANCELED
}
