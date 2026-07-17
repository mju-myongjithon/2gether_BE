package com.twogether.backend.canvas.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "특정 날짜 승인 활동 목록")
public record CanvasDailyActivitiesResponse(
        @Schema(example = "2026-07-17") LocalDate date,
        @Schema(example = "2") long count,
        @Schema(description = "승인 시각 오름차순 활동 목록") List<CanvasActivityResponse> activities
) {
}
