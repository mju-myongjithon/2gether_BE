package com.twogether.backend.gathering.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "모임장(방장) 요약 정보")
public record HostSummaryResponse(

        @Schema(description = "사용자 ID", example = "1")
        Long userId,

        @Schema(description = "닉네임", example = "인준")
        String nickname,

        @Schema(description = "학과명", example = "컴퓨터공학과")
        String departmentName,

        @Schema(description = "캠퍼스", example = "NATURAL", allowableValues = {"HUMANITIES", "NATURAL"})
        String campus

) {
}
