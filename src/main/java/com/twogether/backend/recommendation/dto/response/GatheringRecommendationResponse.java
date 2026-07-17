package com.twogether.backend.recommendation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "모임 AI 팀원 추천 응답")
public record GatheringRecommendationResponse(

        @Schema(
                description = "추천 대상 모임 ID",
                example = "1"
        )
        Long gatheringId,

        @Schema(
                description = "추천된 사용자 수",
                example = "5"
        )
        int recommendationCount,

        @Schema(description = "AI가 추천한 사용자 목록")
        List<RecommendedUserResponse> recommendedUsers

) {

    public GatheringRecommendationResponse {
        recommendedUsers = recommendedUsers == null
                ? List.of()
                : List.copyOf(recommendedUsers);

        recommendationCount = recommendedUsers.size();
    }

    public static GatheringRecommendationResponse of(
            Long gatheringId,
            List<RecommendedUserResponse> recommendedUsers
    ) {
        return new GatheringRecommendationResponse(
                gatheringId,
                recommendedUsers == null ? 0 : recommendedUsers.size(),
                recommendedUsers
        );
    }
}