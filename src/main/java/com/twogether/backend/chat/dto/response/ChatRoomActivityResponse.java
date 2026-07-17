package com.twogether.backend.chat.dto.response;

import com.twogether.backend.chat.domain.ChatActivityStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "채팅방 최근 활동 통계")
public record ChatRoomActivityResponse(
        @Schema(example = "1") Long chatRoomId,
        @Schema(description = "오늘을 포함한 최근 7일 메시지 수", example = "24") long recent7DaysMessageCount,
        @Schema(example = "MEDIUM") ChatActivityStatus activityStatus,
        @Schema(description = "30일 집계 시작일", example = "2026-06-18") LocalDate startDate,
        @Schema(description = "30일 집계 종료일", example = "2026-07-17") LocalDate endDate,
        @Schema(description = "활동이 없는 날짜까지 포함한 오름차순 30일 집계") List<ChatDailyActivityResponse> dailyActivities
) {
}
