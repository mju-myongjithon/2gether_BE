package com.twogether.backend.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "채팅방의 날짜별 활동 집계")
public record ChatDailyActivityResponse(
        @Schema(description = "Asia/Seoul 기준 날짜", example = "2026-07-17") LocalDate date,
        @Schema(description = "집계 대상 메시지 수", example = "8") long messageCount,
        @Schema(description = "프론트엔드 히트맵 표시 단계(0~4)", example = "2") int level
) {
}
