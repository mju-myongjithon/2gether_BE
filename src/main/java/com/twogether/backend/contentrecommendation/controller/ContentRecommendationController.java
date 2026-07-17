package com.twogether.backend.contentrecommendation.controller;

import com.twogether.backend.contentrecommendation.dto.ContentRecommendationResponse;
import com.twogether.backend.contentrecommendation.service.ContentRecommendationService;
import com.twogether.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "맞춤 콘텐츠 추천 API", description = "로그인 사용자 관심사 기반 외부 콘텐츠 카드 추천")
@RestController
@RequestMapping("/api/users/me/content-recommendations")
public class ContentRecommendationController {

    private final ContentRecommendationService contentRecommendationService;

    public ContentRecommendationController(ContentRecommendationService contentRecommendationService) {
        this.contentRecommendationService = contentRecommendationService;
    }

    @Operation(summary = "맞춤 콘텐츠 카드 추천", description = """
            로그인 사용자의 관심사로 블로그, 기사, 행사, 장소, 활동 정보를 추천합니다.
            title, description, url, contentType, matchedTags를 반환하며 카드 클릭 시 외부 URL 이동에 활용합니다.
            기본 환경은 Mock Client이므로 실제 최신 웹 검색을 수행하지 않습니다.
            향후 웹 검색 기능을 지원하는 AI Client로 교체하며 추천 결과는 DB에 저장하지 않습니다.
            """)
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping
    public ResponseEntity<ApiResponse<ContentRecommendationResponse>> recommend(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "6") int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success("맞춤 콘텐츠 추천에 성공했습니다.",
                contentRecommendationService.recommend(jwt.getSubject(), limit)));
    }
}
