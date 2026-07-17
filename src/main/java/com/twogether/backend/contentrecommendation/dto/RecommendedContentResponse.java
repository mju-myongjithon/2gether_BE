package com.twogether.backend.contentrecommendation.dto;

import com.twogether.backend.contentrecommendation.domain.ContentType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "외부 URL로 이동할 수 있는 추천 콘텐츠 카드")
public record RecommendedContentResponse(
        @Schema(example = "content-1") String contentId,
        @Schema(example = "초보자를 위한 Spring Boot 프로젝트 설계") String title,
        @Schema(example = "Spring Boot 프로젝트의 기본 설계 원칙을 소개합니다.") String description,
        @Schema(example = "https://spring.io/guides") String url,
        @Schema(example = "BLOG") ContentType contentType,
        @Schema(example = "[\"백엔드\", \"Spring\"]") List<String> matchedTags
) {
}
