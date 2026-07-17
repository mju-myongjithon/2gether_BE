package com.twogether.backend.gatheringinvitation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 간략 정보")
public record UserBriefResponse(

        @Schema(description = "사용자 ID")
        Long userId,

        @Schema(description = "닉네임")
        String nickname,

        @Schema(description = "학과명")
        String departmentName,

        @Schema(description = "캠퍼스")
        String campus

) {
    public static UserBriefResponse of(
            Long userId,
            String nickname,
            String departmentName,
            String campus
    ) {
        return new UserBriefResponse(userId, nickname, departmentName, campus);
    }
}
