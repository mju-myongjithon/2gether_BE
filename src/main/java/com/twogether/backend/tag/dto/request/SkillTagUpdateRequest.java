package com.twogether.backend.tag.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "사용자 기술 태그 수정 요청")
public record SkillTagUpdateRequest(

        @Schema(
                description = "사용자가 선택한 기술 태그 ID 목록",
                example = "[1, 2, 4]"
        )
        List<Long> tagIds

) {
}