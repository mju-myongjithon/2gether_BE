package com.twogether.backend.gathering.dto.response;

import com.twogether.backend.gathering.domain.Gathering;
import com.twogether.backend.gathering.domain.GatheringMeetingType;
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

        @Schema(description = "모집 시작 일시", example = "2026-07-01T09:00:00+09:00")
        OffsetDateTime recruitStartAt,

        @Schema(description = "모집 종료 일시", example = "2026-07-10T23:59:59+09:00")
        OffsetDateTime recruitEndAt,

        @Schema(
                description = "화면 표시 상태(모집 기간·현재 시각 기준 파생값)",
                example = "RECRUITING",
                allowableValues = {"RECRUITING", "ALWAYS", "UPCOMING", "CLOSED", "CONFIRMED", "COMPLETED", "CANCELED"}
        )
        String displayStatus,

        @Schema(description = "모임 예정 일시", example = "2026-07-15T18:00:00+09:00")
        OffsetDateTime meetAt,

        @Schema(description = "모임 일정 형태", example = "SINGLE")
        GatheringMeetingType meetingType,

        @Schema(description = "모임 종료 일시 또는 종료 날짜", example = "2026-08-15T18:00:00+09:00")
        OffsetDateTime meetingEndAt,

        @Schema(description = "반복 일정 설명", example = "매주 화/목 19:00")
        String repeatRule,

        @Schema(description = "생성 일시", example = "2026-07-09T19:00:00+09:00")
        OffsetDateTime createdAt,

        @Schema(description = "모임장 정보")
        HostSummaryResponse host,

        @Schema(description = "모임 멤버 목록")
        List<GatheringMemberResponse> members,

        @Schema(description = "모임 태그 목록", example = "[\"개발\", \"디자인\"]")
        List<String> tags,

        @Schema(description = "모임 이미지 URL 목록(sort_order 오름차순)")
        List<String> images,

        @Schema(description = "내 신청 상태 (신청한 적이 없으면 null)", example = "PENDING")
        ApplicationStatus myApplicationStatus,

        @Schema(description = "내가 방장인지 여부", example = "false")
        boolean isHost,

        @Schema(description = "내가 멤버인지 여부", example = "false")
        boolean isMember

) {

    /**
     * 모임 상세 응답으로 조립한다.
     *
     * @param gathering            host 가 함께 로딩된 모임 엔티티
     * @param hostDepartmentName   방장 학과명(온보딩 이전이면 null)
     * @param members              멤버 응답 목록
     * @param tags                 태그명 목록
     * @param images               이미지 URL 목록(정렬됨)
     * @param myApplicationStatus  로그인 사용자의 신청 상태(미신청·비로그인이면 null)
     * @param isHost               로그인 사용자가 방장인지
     * @param isMember             로그인 사용자가 멤버인지
     * @param now                  displayStatus 계산 기준 시각
     */
    public static GatheringDetailResponse of(
            Gathering gathering,
            String hostDepartmentName,
            String hostCampus,
            List<GatheringMemberResponse> members,
            List<String> tags,
            List<String> images,
            ApplicationStatus myApplicationStatus,
            boolean isHost,
            boolean isMember,
            OffsetDateTime now
    ) {
        HostSummaryResponse host = new HostSummaryResponse(
                gathering.getHost().getId(),
                gathering.getHost().getNickname(),
                hostDepartmentName,
                hostCampus
        );

        return new GatheringDetailResponse(
                gathering.getId(),
                gathering.getTitle(),
                gathering.getContent(),
                gathering.getCategory().name(),
                gathering.getLocation(),
                gathering.getMaxMembers(),
                members.size(),
                gathering.isFusionEnabled(),
                gathering.getStatus(),
                gathering.getRecruitStartAt(),
                gathering.getRecruitEndAt(),
                gathering.displayStatus(now),
                gathering.getMeetAt(),
                gathering.getMeetingType(),
                gathering.getMeetingEndAt(),
                gathering.getRepeatRule(),
                gathering.getCreatedAt(),
                host,
                members,
                tags,
                images,
                myApplicationStatus,
                isHost,
                isMember
        );
    }
}
