package com.twogether.backend.gathering.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "모임 수정 응답")
public record GatheringUpdateResponse(

        @Schema(description = "모임 ID", example = "1")
        Long gatheringId,

        @Schema(description = "수정 일시", example = "2026-07-09T20:00:00+09:00")
        OffsetDateTime updatedAt

) {
}
