package com.twogether.backend.canvas.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "최근 1년 캠퍼스 잔디 응답")
public record CanvasContributionResponse(
        @Schema(example = "2025-07-18") LocalDate startDate,
        @Schema(example = "2026-07-17") LocalDate endDate,
        @Schema(description = "기간 내 AI 승인 활동 합계", example = "37") long totalApprovedCount,
        @Schema(description = "활동이 없는 날을 포함한 365개 날짜") List<CanvasContributionDayResponse> days
) {
}
