package com.twogether.backend.gathering.dto.response;

import com.twogether.backend.gathering.domain.GatheringStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "모임 완료 응답")
public record GatheringCompleteResponse(
        @Schema(description = "모임 ID", example = "1")
        Long gatheringId,

        @Schema(description = "모임 상태", example = "COMPLETED")
        GatheringStatus status
) {
}
