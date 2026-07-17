package com.twogether.backend.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "안심 커넥트 코드 입장 요청")
public record QuickConnectJoinRequest(

        @Schema(description = "6자리 안심 커넥트 코드", example = "482915")
        @NotBlank(message = "코드는 필수입니다.")
        @Size(min = 6, max = 6, message = "코드는 6자리여야 합니다.")
        String code

) {
}
