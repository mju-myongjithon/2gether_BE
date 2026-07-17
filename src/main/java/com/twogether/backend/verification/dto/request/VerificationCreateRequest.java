package com.twogether.backend.verification.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerificationCreateRequest(
        @Schema(description = "활동 인증 사진 URL")
        @NotBlank(message = "사진 URL은 필수입니다.")
        @Size(max = 300, message = "사진 URL은 300자 이하여야 합니다.")
        String photoUrl,

        @Schema(description = "활동 후기")
        @Size(max = 200, message = "활동 후기는 200자 이하여야 합니다.")
        String reviewText
) {
}
