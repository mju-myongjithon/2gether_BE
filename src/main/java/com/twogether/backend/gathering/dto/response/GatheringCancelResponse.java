package com.twogether.backend.gathering.dto.response;

import com.twogether.backend.gathering.domain.GatheringStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "모임 취소 응답")
public record GatheringCancelResponse(

        @Schema(description = "모임 ID", example = "1")
        Long gatheringId,

        @Schema(description = "모임 상태", example = "CANCELED")
        GatheringStatus status,

        @Schema(description = "취소 일시", example = "2026-07-09T20:10:00+09:00")
        OffsetDateTime canceledAt

) {
}
