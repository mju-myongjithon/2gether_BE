package com.twogether.backend.gathering.dto.response;

import com.twogether.backend.gathering.domain.Gathering;
import com.twogether.backend.gathering.domain.GatheringStatus;
import com.twogether.backend.gatheringmember.domain.GatheringMemberRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "내가 속한 모임 응답")
public record MyGatheringResponse(

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

        @Schema(description = "모임 상태", example = "RECRUITING")
        GatheringStatus status,

        @Schema(description = "모임 예정 일시", example = "2026-07-15T18:00:00+09:00")
        OffsetDateTime meetAt,

        @Schema(description = "내 역할", example = "HOST")
        GatheringMemberRole role,

        @Schema(description = "모임 태그 목록", example = "[\"개발\", \"디자인\"]")
        List<String> tags
) {

    public static MyGatheringResponse of(
            Gathering gathering,
            int currentMemberCount,
            GatheringMemberRole role,
            List<String> tags
    ) {
        return new MyGatheringResponse(
                gathering.getId(),
                gathering.getTitle(),
                gathering.getCategory().name(),
                gathering.getLocation(),
                gathering.getMaxMembers(),
                currentMemberCount,
                gathering.getStatus(),
                gathering.getMeetAt(),
                role,
                tags
        );
    }
}