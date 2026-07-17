package com.twogether.backend.contentrecommendation.client;

import java.util.List;

/** 웹 검색 AI에 전달할 제공업체 독립 입력 계약이다. */
public record ContentRecommendationContext(
        Long userId,
        List<String> interestTags,
        String campus,
        Integer limit
) {
}
