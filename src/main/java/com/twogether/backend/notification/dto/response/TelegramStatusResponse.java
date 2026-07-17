package com.twogether.backend.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "텔레그램 연결 상태 응답")
public record TelegramStatusResponse(

        @Schema(description = "연결 여부", example = "true")
        boolean linked,

        @Schema(description = "연결된 텔레그램 chat_id (미연결 시 null)", example = "123456789")
        Long telegramChatId,

        @Schema(description = "연결 일시 (미연결 시 null)", example = "2026-07-17T16:00:00+09:00")
        OffsetDateTime linkedAt

) {

    public static TelegramStatusResponse linked(Long chatId, OffsetDateTime linkedAt) {
        return new TelegramStatusResponse(true, chatId, linkedAt);
    }

    public static TelegramStatusResponse notLinked() {
        return new TelegramStatusResponse(false, null, null);
    }
}
