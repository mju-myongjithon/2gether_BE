package com.twogether.backend.gathering.dto.response;

import com.twogether.backend.gathering.domain.Gathering;
import com.twogether.backend.gathering.domain.GatheringStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "모임 목록 항목 응답")
public record GatheringSummaryResponse(

        @Schema(description = "모임 ID", example = "1")
        Long gatheringId,

        @Schema(description = "모임 제목", example = "인문X자연 해커톤 팀 모집")
        String title,

        @Schema(description = "모임 카테고리", example = "HACKATHON")
        String category,

        @Schema(description = "모임 장소", example = "자연캠 명진당")
        String location,

        @Schema(description = "최대 인원", example = "6")
        int maxMembers,

        @Schema(description = "현재 참여 인원", example = "2")
        int currentMemberCount,

        @Schema(description = "인문X자연 융합 모임 여부", example = "true")
        boolean fusionEnabled,

        @Schema(description = "저장 상태(RECRUITING/CONFIRMED/COMPLETED/CANCELED)", example = "RECRUITING")
        GatheringStatus status,

        @Schema(
                description = "화면 표시 상태(모집 기간·현재 시각 기준 파생값)",
                example = "RECRUITING",
                allowableValues = {"RECRUITING", "ALWAYS", "UPCOMING", "CLOSED", "CONFIRMED", "COMPLETED", "CANCELED"}
        )
        String displayStatus,

        @Schema(description = "모임 예정 일시", example = "2026-07-15T18:00:00+09:00")
        OffsetDateTime meetAt,

        @Schema(description = "모임 태그 목록", example = "[\"개발\", \"디자인\"]")
        List<String> tags,

        @Schema(description = "모임장 정보")
        HostSummaryResponse host

) {

    /**
     * 목록 카드 응답으로 변환한다.
     *
     * @param gathering          host 가 함께 로딩된 모임 엔티티
     * @param tags               해당 모임의 태그명 목록(배치 조회 결과)
     * @param hostDepartmentName 방장 학과명(배치 조회 결과, 온보딩 이전이면 null)
     * @param now                displayStatus 계산 기준 시각
     */
    public static GatheringSummaryResponse of(
            Gathering gathering,
            List<String> tags,
            String hostDepartmentName,
            OffsetDateTime now
    ) {
        HostSummaryResponse host = new HostSummaryResponse(
                gathering.getHost().getId(),
                gathering.getHost().getNickname(),
                hostDepartmentName,
                // 캠퍼스 비율 기능 제외 → campus 는 null 처리(설계 확정)
                null
        );

        return new GatheringSummaryResponse(
                gathering.getId(),
                gathering.getTitle(),
                gathering.getCategory().name(),
                gathering.getLocation(),
                gathering.getMaxMembers(),
                gathering.getCurrentMembers(),
                gathering.isFusionEnabled(),
                gathering.getStatus(),
                gathering.displayStatus(now),
                gathering.getMeetAt(),
                tags,
                host
        );
    }
}
