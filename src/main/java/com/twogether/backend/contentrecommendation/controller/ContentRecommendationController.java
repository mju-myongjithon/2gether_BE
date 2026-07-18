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
            로그인 사용자의 관심사를 기반으로 콘텐츠를 추천합니다.
            설정에 따라 Mock 또는 Gemini Google Search 기반 Client를 사용합니다.
            추천 결과는 저장하지 않으며 카드의 URL을 통해 외부 콘텐츠로 이동할 수 있습니다.
            """)
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping
    public ResponseEntity<ApiResponse<ContentRecommendationResponse>> recommend(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "6") int limit) {
        return ResponseEntity.ok(ApiResponse.success("맞춤 콘텐츠 추천에 성공했습니다.",
                contentRecommendationService.recommend(jwt.getSubject(), limit)));
    }
}
