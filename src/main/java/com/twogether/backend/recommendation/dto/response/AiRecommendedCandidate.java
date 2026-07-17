package com.twogether.backend.recommendation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI가 추천한 후보 사용자 정보")
public record AiRecommendedCandidate(

        @Schema(
                description = "추천 사용자 고유 ID",
                example = "14"
        )
        Long userId,

        @Schema(
                description = "AI 추천 점수",
                example = "91"
        )
        int score,

        @Schema(
                description = "AI 추천 이유",
                example = "Spring Boot 기술 태그와 해커톤 관심사가 모임 목적과 잘 맞습니다."
        )
        String reason

) {

    public AiRecommendedCandidate {
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException(
                    "추천 점수는 0점 이상 100점 이하여야 합니다."
            );
        }

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(
                    "추천 이유는 필수입니다."
            );
        }
    }
}