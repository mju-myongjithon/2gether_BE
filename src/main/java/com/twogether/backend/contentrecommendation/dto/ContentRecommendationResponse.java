package com.twogether.backend.contentrecommendation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "맞춤 콘텐츠 카드 추천 응답")
public record ContentRecommendationResponse(
        @Schema(description = "관심사 연관도가 높은 순서의 콘텐츠 카드")
        List<RecommendedContentResponse> recommendations
) {
}
