package com.twogether.backend.gatheringapplication.dto.response;

import com.twogether.backend.gathering.domain.GatheringStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "모임 요약 정보")
public record GatheringBriefResponse(

        @Schema(description = "모임 ID", example = "1")
        Long gatheringId,

        @Schema(description = "모임 제목", example = "인문X자연 해커톤 팀 모집")
        String title,

        @Schema(description = "모임 카테고리", example = "해커톤")
        String category,

        @Schema(description = "모임 상태", example = "RECRUITING")
        GatheringStatus status

) {
}
