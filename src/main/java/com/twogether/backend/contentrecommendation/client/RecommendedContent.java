package com.twogether.backend.contentrecommendation.client;

import com.twogether.backend.contentrecommendation.domain.ContentType;

import java.util.List;

/** 웹 검색 AI가 검색, 선별, 요약하여 반환할 제공업체 독립 결과 계약이다. */
public record RecommendedContent(
        String title,
        String description,
        String url,
        ContentType contentType,
        List<String> matchedTags
) {
}
