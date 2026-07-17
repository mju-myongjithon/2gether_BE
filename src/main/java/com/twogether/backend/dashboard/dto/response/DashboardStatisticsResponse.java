package com.twogether.backend.dashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이번 달 라이브 유대감 통계")
public record DashboardStatisticsResponse(
        @Schema(description = "기준 월", example = "2026-07") String month,
        @Schema(description = "이번 달 AI 승인 활동 수", example = "24") long monthlyInteractionCount,
        @Schema(description = "이번 달 확정된 유효 모임 수", example = "18") long matchedGroupCount,
        @Schema(description = "승인 활동 모임의 고유 참여자 수", example = "63") long participantCount
) {
}
