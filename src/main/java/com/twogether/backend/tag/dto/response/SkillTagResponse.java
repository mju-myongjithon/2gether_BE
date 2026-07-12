package com.twogether.backend.tag.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "기술 태그 정보 응답")
public record SkillTagResponse(

        @Schema(
                description = "기술 태그 고유 ID",
                example = "1"
        )
        Long id,

        @Schema(
                description = "기술 태그 이름",
                example = "Spring Boot"
        )
        String name

) {
}