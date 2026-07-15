package com.twogether.backend.gathering.dto.response;

import com.twogether.backend.gathering.domain.GatheringStatus;
import com.twogether.backend.gathering.domain.RecruitPhase;
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

        @Schema(description = "모임 상태 (DB 저장값)", example = "RECRUITING")
        GatheringStatus status,

        @Schema(description = "모집 진행 단계 (계산값)", example = "OPEN")
        RecruitPhase recruitPhase,

        @Schema(description = "모집 시작 일시", example = "2026-07-10T00:00:00+09:00", nullable = true)
        OffsetDateTime recruitStartAt,

        @Schema(description = "모집 마감 일시 (없으면 상시 모집)", example = "2026-07-14T23:59:59+09:00", nullable = true)
        OffsetDateTime recruitEndAt,

        @Schema(description = "모임 예정 일시", example = "2026-07-15T18:00:00+09:00")
        OffsetDateTime meetAt,

        @Schema(description = "생성 일시", example = "2026-07-09T19:00:00+09:00")
        OffsetDateTime createdAt,

        @Schema(description = "모임 이미지 목록 (최대 5개, sortOrder 순)")
        List<GatheringImageResponse> images,

        @Schema(description = "모임장 정보")
        HostSummaryResponse