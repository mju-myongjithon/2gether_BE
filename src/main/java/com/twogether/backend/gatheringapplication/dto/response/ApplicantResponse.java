package com.twogether.backend.gatheringapplication.dto.response;

import com.twogether.backend.tag.dto.response.HobbyTagResponse;
import com.twogether.backend.tag.dto.response.SkillTagResponse;
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

        @Schema(description = "캠퍼스", example = "HUMANITIES", allowableValues = {"HUMANITIES", "NATURAL"})
        String campus,

        @Schema(description = "취미 태그 목록")
        List<HobbyTagResponse> hobbyTags,

        @Schema(description = "기술 태그 목록")
        List<SkillTagResponse> skillTags

) {
}
