package com.twogether.backend.gathering.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "모임 일정 형태")
public enum GatheringMeetingType {

    @Schema(description = "단발성 일정")
    SINGLE,

    @Schema(description = "기간형 일정")
    PERIOD,

    @Schema(description = "반복형 일정")
    REPEAT
}