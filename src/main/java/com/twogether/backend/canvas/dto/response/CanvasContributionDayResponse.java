package com.twogether.backend.canvas.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "날짜별 캠퍼스 잔디 항목")
public record CanvasContributionDayResponse(
        @Schema(description = "한국 시간 기준 날짜", example = "2026-07-17") LocalDate date,
        @Schema(description = "AI 승인 활동 수", example = "2") long count,
        @Schema(description = "잔디 레벨(0~4)", example = "2") int level
) {
}
