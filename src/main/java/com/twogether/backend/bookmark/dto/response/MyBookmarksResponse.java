package com.twogether.backend.bookmark.dto.response;

import com.twogether.backend.gathering.dto.response.GatheringSummaryResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "내 북마크 목록 응답")
public record MyBookmarksResponse(

        @Schema(description = "북마크한 사용자 목록")
        List<BookmarkedUserResponse> users,

        @Schema(description = "북마크한 모임 목록")
        List<GatheringSummaryResponse> gatherings
) {
}