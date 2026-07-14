package com.twogether.backend.chat.dto.request;

import com.twogether.backend.chat.domain.MessageType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "메시지 전송 요청")
public record ChatMessageSendRequest(

        @Schema(description = "메시지 타입", example = "TEXT")
        MessageType type,

        @Schema(description = "메시지 본문", example = "안녕하세요!")
        String content

) {
}
