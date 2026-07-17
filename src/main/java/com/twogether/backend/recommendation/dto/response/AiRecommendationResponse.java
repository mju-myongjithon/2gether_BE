package com.twogether.backend.recommendation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "AI 팀원 추천 응답")
public record AiRecommendationResponse(

        @Schema(description = "AI가 추천한 후보 사용자 목록")
        List<AiRecommendedCandidate> recommendations

) {

    public AiRecommendationResponse {
        recommendations = recommendations == null
                ? List.of()
                : List.copyOf(recommendations);
    }
}