package com.twogether.backend.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "안심 커넥트 방/코드 생성 응답")
public record QuickConnectCreateResponse(

        @Schema(description = "생성된 채팅방 ID", example = "21")
        Long roomId,

        @Schema(description = "6자리 안심 커넥트 코드", example = "482915")
        String code,

        @Schema(description = "코드 만료 일시", example = "2026-07-18T10:00:00+09:00")
        OffsetDateTime expiresAt

) {
}
