package com.twogether.backend.gathering.dto.response;

import com.twogether.backend.gathering.domain.GatheringStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "모임 생성 응답")
public record GatheringCreateResponse(

        @Schema(description = "모임 ID", example = "1")
        Long gatheringId,

        @Schema(description = "방장(생성자) ID", example = "1")
        Long hostId,

        @Schema(description = "모임 제목", example = "인문X자연 해커톤 팀 모집")
        String title,

        @Schema(description = "모임 상태", example = "RECRUITING")
        GatheringStatus status,

        @Schema(description = "생성 일시", example = "2026-07-09T19:00:00+09:00")
        OffsetDateTime createdAt

) {
}
