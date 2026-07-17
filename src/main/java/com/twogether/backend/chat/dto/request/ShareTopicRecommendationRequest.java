package com.twogether.backend.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "추천 대화 주제 공유 요청")
public record ShareTopicRecommendationRequest(
        @NotBlank @Size(max = 80) @Schema(example = "각자의 역할과 강점") String title,
        @NotBlank @Size(max = 500) @Schema(example = "자신 있는 부분과 맡아 보고 싶은 역할을 이야기해 보세요.") String content
) {
}
