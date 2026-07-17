package com.twogether.backend.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "안심 커넥트 입장 응답")
public record QuickConnectJoinResponse(

        @Schema(description = "입장한 채팅방 ID", example = "21")
        Long roomId

) {
}
