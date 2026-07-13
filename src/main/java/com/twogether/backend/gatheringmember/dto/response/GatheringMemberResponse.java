package com.twogether.backend.gatheringmember.dto.response;

import com.twogether.backend.gatheringmember.domain.GatheringMemberRole;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "모임 멤버 정보")
public record GatheringMemberResponse(

        @Schema(description = "사용자 ID", example = "1")
        Long userId,

        @Schema(description = "닉네임", example = "인준")
        String nickname,

        @Schema(description = "역할", example = "HOST")
        GatheringMemberRole role,

        @Schema(description = "학과명", example = "컴퓨터공학과")
        String departmentName,

        @Schema(description = "캠퍼스", example = "자연캠", allowableValues = {"인문캠", "자연캠"})
        String campus

) {
}
