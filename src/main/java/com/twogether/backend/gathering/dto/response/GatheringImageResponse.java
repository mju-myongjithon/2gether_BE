package com.twogether.backend.gathering.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "모임 이미지 정보")
public record GatheringImageResponse(

        @Schema(description = "이미지 ID", example = "1")
        Long id,

        @Schema(description = "이미지 URL", example = "https://cdn.2gether.app/gatherings/1/1.png")
        String imageUrl,

        @Schema(description = "노출 순서 (0부터 시작)", example = "0")
        int sortOrder

) {
}
