package com.twogether.backend.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 읽음 처리 실시간 브로드캐스트 페이로드.
 *
 * markRead 로 last_read_message_id 가 전진했을 때
 * /sub/chat/rooms/{roomId}/read 로 발행된다.
 * 클라이언트는 (previousLastReadMessageId, lastReadMessageId] 구간 메시지의
 * readByCount 를 1 증가시켜 화면의 안읽음 수를 갱신한다.
 */
@Schema(description = "읽음 처리 브로드캐스트")
public record ChatReadBroadcastResponse(

        @Schema(description = "채팅방 ID", example = "10")
        Long roomId,

        @Schema(description = "읽음 처리한 사용자 ID", example = "1")
        Long userId,

        @Schema(description = "갱신 전 마지막 읽은 메시지 ID (없었으면 0)", example = "100")
        Long previousLastReadMessageId,

        @Schema(description = "갱신 후 마지막 읽은 메시지 ID", example = "105")
        Long lastReadMessageId
) {
}
