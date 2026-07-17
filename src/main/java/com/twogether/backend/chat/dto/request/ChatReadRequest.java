package com.twogether.backend.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "메시지 읽음 처리 요청")
public record ChatReadRequest(

        @Schema(description = "마지막으로 읽은 메시지 ID", example = "106")
        @NotNull(message = "마지막으로 읽은 메시지 ID는 필수입니다.")
        Long lastReadMessageId

) {
}
