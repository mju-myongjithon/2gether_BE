package com.twogether.backend.gathering.dto.response;

import com.twogether.backend.gathering.domain.GatheringStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "모임 목록 항목 응답")
public record GatheringSummaryResponse(

        @Schema(description = "모임 ID", example = "1")
        Long gatheringId,

        @Schema(description = "모임 제목", example = "인문X자연 해커톤 팀 모집")
        String title,

        @Schema(description = "모임 카테고리", example = "해커톤")
        String category,

        @Schema(description = "모임 장소", example = "자연캠 명진당")
        String location,

        @Schema(description = "최대 인원", example = "6")
        int maxMembers,

        @Schema(description = "현재 참여 인원", example = "2")
        int currentMemberCount,

        @Schema(description = "인문X자연 융합 모임 여부", example = "true")
        boolean fusionEnabled,

        @Schema(description = "모임 상태", example = "RECRUITING")
        GatheringStatus status,

        @Schema(description = "모임 예정 일시", example = "2026-07-15T18:00:00+09:00")
        OffsetDateTime meetAt,

        @Schema(description = "모임장 정보")
        HostSummaryResponse host

) {
}
