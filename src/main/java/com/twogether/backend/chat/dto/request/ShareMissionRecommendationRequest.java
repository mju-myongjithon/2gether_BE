package com.twogether.backend.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "추천 모임 미션 공유 요청")
public record ShareMissionRecommendationRequest(
        @NotBlank @Size(max = 80) @Schema(description = "미션 제목", example = "공통점 3가지 찾기") String title,
        @NotBlank @Size(max = 500) @Schema(description = "미션 수행 안내", example = "참여자들이 대화를 나누며 서로의 공통점 3가지를 찾아보세요.") String content
) {
}
