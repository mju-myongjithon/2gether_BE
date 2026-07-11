package com.twogether.backend.emailverification.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "학교 이메일 인증번호 확인 요청")
public record EmailVerificationConfirmRequest(

        @Schema(
                description = "인증할 명지대학교 이메일",
                example = "student@mju.ac.kr"
        )
        String email,

        @Schema(
                description = "이메일로 전달받은 6자리 인증번호",
                example = "123456"
        )
        String code

) {
}