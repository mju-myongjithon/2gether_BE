package com.twogether.backend.recommendation.controller;

import com.twogether.backend.global.response.ApiResponse;
import com.twogether.backend.recommendation.dto.request.AiGatheringRecommendationRequest;
import com.twogether.backend.recommendation.dto.response.AiRecommendationResponse;
import com.twogether.backend.recommendation.service.GatheringRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;

@Tag(
        name = "AI 팀원 추천 API",
        description = "모임 정보를 기반으로 적합한 팀원 후보를 추천하는 API"
)
@RestController
@RequestMapping("/api/gatherings")
public class GatheringRecommendationController {

    private final GatheringRecommendationService recommendationService;

    public GatheringRecommendationController(
            GatheringRecommendationService recommendationService
    ) {
        this.recommendationService = recommendationService;
    }

    @Operation(
            summary = "AI 팀원 추천",
            description = """
                    모임 정보와 후보 사용자 목록을 기반으로
                    AI 팀원 추천 결과를 반환합니다.

                    현재는 실제 AI 연동 전 단계이므로
                    태그 일치 개수를 기준으로 Mock 추천 결과를 생성합니다.

                    추후 백엔드에서 모임과 후보 정보를 직접 조회하도록 변경할 예정입니다.
                    """
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/{gatheringId}/recommendations")
    public ResponseEntity<ApiResponse<AiRecommendationResponse>> recommendMembers(
            @PathVariable Long gatheringId,
            @Valid @RequestBody AiGatheringRecommendationRequest request
    ) {
        validateGatheringId(gatheringId, request);

        AiRecommendationResponse response =
                recommendationService.recommend(
                        gatheringId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "AI 팀원 추천에 성공했습니다.",
                        response
                )
        );
    }

    private void validateGatheringId(
            Long gatheringId,
            AiGatheringRecommendationRequest request
    ) {
        if (request == null
                || request.gathering() == null
                || !gatheringId.equals(
                request.gathering().gatheringId()
        )) {
            throw new BusinessException(
                    ErrorCode.RECOMMENDATION_GATHERING_MISMATCH
            );
        }
    }
}