package com.twogether.backend.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "추천 대화 주제")
public record RecommendedTopicResponse(
        @Schema(example = "topic-1") String topicId,
        @Schema(example = "각자의 역할과 강점") String title,
        @Schema(example = "자신 있는 부분과 맡아 보고 싶은 역할을 이야기해 보세요.") String content
) {
}
