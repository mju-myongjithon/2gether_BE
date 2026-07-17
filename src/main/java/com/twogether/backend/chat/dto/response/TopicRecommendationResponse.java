package com.twogether.backend.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "채팅방 대화 주제 추천 결과")
public record TopicRecommendationResponse(
        @Schema(example = "1") Long chatRoomId,
        List<RecommendedTopicResponse> topics
) {
}
