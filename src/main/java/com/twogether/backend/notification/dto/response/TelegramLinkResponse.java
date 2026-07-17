package com.twogether.backend.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "텔레그램 연결용 딥링크 응답")
public record TelegramLinkResponse(

        @Schema(description = "봇 연결 딥링크. 사용자가 눌러 /start 하면 연결됩니다.",
                example = "https://t.me/udaegam_bot?start=ab12cd34...")
        String deepLink,

        @Schema(description = "연결 코드(딥링크에 포함)", example = "ab12cd34ef56...")
        String code,

        @Schema(description = "코드 만료 일시", example = "2026-07-17T16:10:00+09:00")
        OffsetDateTime expiresAt

) {
}
