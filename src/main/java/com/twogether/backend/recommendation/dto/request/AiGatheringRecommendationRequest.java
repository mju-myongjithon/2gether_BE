package com.twogether.backend.recommendation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "AI 팀원 추천 요청 정보")
public record AiGatheringRecommendationRequest(

        @Schema(description = "추천 대상 모임 정보")
        AiGatheringInfo gathering,

        @Schema(description = "AI가 평가할 후보 사용자 목록")
        List<AiCandidateInfo> candidates,

        @Schema(
                description = "AI가 최종 추천할 사용자 수",
                example = "5"
        )
        int recommendationCount

) {

    public AiGatheringRecommendationRequest {
        candidates = candidates == null
                ? List.of()
                : List.copyOf(candidates);

        if (recommendationCount < 1) {
            throw new IllegalArgumentException(
                    "추천 인원은 1명 이상이어야 합니다."
            );
        }

        if (recommendationCount > candidates.size()) {
            recommendationCount = candidates.size();
        }
    }
}