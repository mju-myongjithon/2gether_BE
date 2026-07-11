package com.twogether.backend.global.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공통 에러 응답")
public record ErrorResponse(

        @Schema(
                description = "요청 성공 여부",
                example = "false"
        )
        boolean success,

        @Schema(
                description = "클라이언트가 에러 종류를 구분하기 위한 코드",
                example = "DUPLICATE_NICKNAME"
        )
        String code,

        @Schema(
                description = "에러 메시지",
                example = "이미 사용 중인 닉네임입니다."
        )
        String message
) {

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(false, code, message);
    }
}