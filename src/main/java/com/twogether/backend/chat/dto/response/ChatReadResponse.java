package com.twogether.backend.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "메시지 읽음 처리 응답")
public record ChatReadResponse(

        @Schema(description = "채팅방 ID", example = "10")
        Long roomId,

        @Schema(description = "마지막으로 읽은 메시지 ID", example = "106")
        Long lastReadMessageId,

        @Schema(description = "읽지 않은 메시지 수", example = "0")
        int unreadCount

) {
}
