package com.twogether.backend.gathering.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "모임 카테고리")
public enum GatheringCategory {

    @Schema(description = "스터디")
    STUDY,

    @Schema(description = "취미")
    HOBBY,

    @Schema(description = "해커톤")
    HACKATHON,

    @Schema(description = "프로젝트")
    PROJECT,

    @Schema(description = "네트워킹")
    NETWORKING
}
