package com.twogether.backend.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "타이핑 상태 요청")
public record TypingStatusRequest(

        @Schema(description = "타이핑 여부", example = "true")
        boolean isTyping

) {
}
