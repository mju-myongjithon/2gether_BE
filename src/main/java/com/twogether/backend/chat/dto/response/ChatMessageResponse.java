package com.twogether.backend.chat.dto.response;

import com.twogether.backend.chat.domain.MessageType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "채팅 메시지 응답")
public record ChatMessageResponse(

        @Schema(description = "메시지 ID", example = "105")
        Long messageId,

        @Schema(description = "채팅방 ID", example = "10")
        Long roomId,

        @Schema(description = "발신자 ID (SYSTEM 메시지는 null)", example = "1")
        Long senderId,

        @Schema(description = "발신자 닉네임 (SYSTEM 메시지는 null)", example = "인준")
        String senderNickname,

        @Schema(description = "메시지 타입", example = "TEXT")
        MessageType type,

        @Schema(description = "메시지 본문", example = "내일 7시에 봬요!")
        String content,

        @Schema(description = "전송 일시", example = "2026-07-13T21:10:00+09:00")
        OffsetDateTime createdAt

) {
}
