package com.twogether.backend.gathering.dto.response;

import com.twogether.backend.gathering.domain.GatheringStatus;
import com.twogether.backend.gatheringapplication.domain.ApplicationStatus;
import com.twogether.backend.gatheringmember.dto.response.GatheringMemberResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "모임 상세 응답")
public record GatheringDetailResponse(

        @Schema(description = "모임 ID", example = "1")
        Long gatheringId,

        @Schema(description = "모임 제목", example = "인문X자연 해커톤 팀 모집")
        String title,

        @Schema(description = "모임 소개", example = "기획, 디자인, 개발 같이 할 사람 구합니다.")
        String content,

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

        @Schema(description = "생성 일시", example = "2026-07-09T19:00:00+09:00")
        OffsetDateTime createdAt,

        @Schema(description = "모임장 정보")
        HostSummaryResponse host,

        @Schema(description = "모임 멤버 목록")
        List<GatheringMemberResponse> members,

        @Schema(description = "참여 인원 캠퍼스 비율")
        CampusRatioResponse campusRatio,

        @Schema(description = "내 신청 상태 (신청한 적이 없으면 null)", example = "PENDING")
        ApplicationStatus myApplicationStatus,

        @Schema(description = "내가 방장인지 여부", example = "false")
        boolean isHost,

        @Schema(description = "내가 멤버인지 여부", example = "false")
        boolean isMember

) {
}
