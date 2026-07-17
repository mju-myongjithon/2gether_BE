package com.twogether.backend.bookmark.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "북마크 상태 응답")
public record BookmarkStateResponse(

        @Schema(description = "북마크 여부", example = "true")
        boolean bookmarked
) {
}