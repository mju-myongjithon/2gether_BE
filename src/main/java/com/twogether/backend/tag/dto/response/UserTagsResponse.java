package com.twogether.backend.tag.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "사용자 선택 태그 조회 응답")
public record UserTagsResponse(

        @Schema(
                description = "사용자가 선택한 취미 태그 목록"
        )
        List<HobbyTagResponse> hobbyTags,

        @Schema(
                description = "사용자가 선택한 기술 태그 목록"
        )
        List<SkillTagResponse> skillTags
) {
}