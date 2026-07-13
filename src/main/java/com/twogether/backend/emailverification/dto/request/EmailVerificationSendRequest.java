package com.twogether.backend.emailverification.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "학교 이메일 인증번호 발송 요청")
public record EmailVerificationSendRequest(

        @Schema(
                description = "인증번호를 받을 명지대학교 이메일",
                example = "student@mju.ac.kr"
        )
        String email

) {
}