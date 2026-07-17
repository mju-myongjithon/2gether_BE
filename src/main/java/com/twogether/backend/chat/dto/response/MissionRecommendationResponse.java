package com.twogether.backend.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "채팅방 모임 미션 추천 결과")
public record MissionRecommendationResponse(
        @Schema(description = "채팅방 ID", example = "1") Long chatRoomId,
        @Schema(description = "추천 미션 1~3개") List<RecommendedMissionResponse> missions
) {
}
