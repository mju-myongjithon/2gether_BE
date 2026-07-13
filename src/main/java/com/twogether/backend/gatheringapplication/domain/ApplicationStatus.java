package com.twogether.backend.gatheringapplication.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "모임 신청 상태")
public enum ApplicationStatus {

    @Schema(description = "심사 대기 중")
    PENDING,

    @Schema(description = "수락됨")
    ACCEPTED,

    @Schema(description = "거절됨")
    REJECTED
}
