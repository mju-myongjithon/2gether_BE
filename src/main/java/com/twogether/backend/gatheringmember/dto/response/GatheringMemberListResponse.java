package com.twogether.backend.gatheringmember.dto.response;

import com.twogether.backend.gatheringmember.domain.GatheringMemberRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "모임 멤버 목록 항목 응답")
public record GatheringMemberListResponse(

        @Schema(description = "사용자 ID", example = "1")
        Long userId,

        @Schema(description = "닉네임", example = "인준")
        String nickname,

        @Schema(description = "역할", example = "HOST")
        GatheringMemberRole role,

        @Schema(description = "학과명", example = "컴퓨터공학과")
        String departmentName,

        @Schema(description = "캠퍼스", example = "NATURAL", allowableValues = {"HUMANITIES", "NATURAL"})
        String campus,

        @Schema(description = "모임 참여 일시", example = "2026-07-09T19:00:00+09:00")
        OffsetDateTime joinedAt

) {
}
