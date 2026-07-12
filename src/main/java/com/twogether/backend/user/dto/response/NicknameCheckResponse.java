package com.twogether.backend.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "닉네임 중복 확인 응답")
public record NicknameCheckResponse(

        @Schema(
                description = "닉네임 사용 가능 여부. true이면 사용할 수 있습니다.",
                example = "true"
        )
        boolean available,

        @Schema(
                description = "확인한 닉네임",
                example = "인준"
        )
        String nickname

) {
}