package com.twogether.backend.gathering.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "모임 참여 인원의 캠퍼스 비율")
public record CampusRatioResponse(

        @Schema(description = "인문캠 인원 수", example = "1")
        int humanitiesCampusCount,

        @Schema(description = "자연캠 인원 수", example = "1")
        int naturalCampusCount

) {
}
