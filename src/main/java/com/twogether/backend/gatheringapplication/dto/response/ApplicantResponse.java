package com.twogether.backend.gatheringapplication.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "신청자 정보")
public record ApplicantResponse(

        @Schema(description = "사용자 ID", example = "2")
        Long userId,

        @Schema(description = "닉네임", example = "기획러")
        String nickname,

        @Schema(description = "학과명", example = "경영학과")
        String departmentName,

        @Schema(description = "캠퍼스", example = "인문캠", allowableValues = {"인문캠", "자연캠"})
        String campus,

        @Schema(description = "취미 태그 목록", example = "[\"맛집 탐방\"]")
        List<String> hobbyTags,

        @Schema(description = "기술 태그 목록", example = "[\"기획\", \"발표\"]")
        List<String> skillTags

) {
}
