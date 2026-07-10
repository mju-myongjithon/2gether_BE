package com.twogether.backend.global.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공통 API 응답")
public record ApiResponse<T>(

        @Schema(description = "요청 성공 여부", example = "true")
        boolean success,

        @Schema(description = "응답 메시지", example = "요청이 성공했습니다.")
        String message,

        @Schema(description = "응답 데이터")
        T data
) {

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(true, message, null);
    }
}