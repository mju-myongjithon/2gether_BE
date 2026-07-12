package com.twogether.backend.tag.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "취미 태그 정보 응답")
public record HobbyTagResponse(

        @Schema(
                description = "취미 태그 고유 ID",
                example = "1"
        )
        Long id,

        @Schema(
                description = "취미 태그 이름",
                example = "보드게임"
        )
        String name

) {
}