package com.twogether.backend.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "추천 모임 미션")
public record RecommendedMissionResponse(
        @Schema(description = "응답 내 미션 식별자", example = "mission-1") String missionId,
        @Schema(description = "미션 제목", example = "공통점 3가지 찾기") String title,
        @Schema(description = "미션 수행 안내", example = "참여자들이 대화를 나누며 서로의 공통점 3가지를 찾아보세요.") String content,
        @Schema(description = "미션 난이도", example = "EASY") String difficulty
) {
}
