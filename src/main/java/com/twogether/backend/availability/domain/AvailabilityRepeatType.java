package com.twogether.backend.availability.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가용 일정 반복 유형")
public enum AvailabilityRepeatType {

    @Schema(description = "매주 반복")
    EVERY_WEEK,

    @Schema(description = "홀수 주 반복")
    ODD_WEEK,

    @Schema(description = "짝수 주 반복")
    EVEN_WEEK
}