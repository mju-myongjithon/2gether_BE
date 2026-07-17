package com.twogether.backend.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "타이핑 상태 응답")
public record TypingStatusResponse(

        @Schema(description = "사용자 ID", example = "1")
        Long userId,

        @Schema(description = "사용자 닉네임", example = "인준")
        String nickname,

        @Schema(description = "타이핑 여부", example = "true")
        boolean isTyping

) {
}
