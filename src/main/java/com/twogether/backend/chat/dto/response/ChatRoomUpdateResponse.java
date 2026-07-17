package com.twogether.backend.chat.dto.response;

import com.twogether.backend.chat.domain.MessageType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

/**
 * 채팅방 목록 실시간 갱신용 SSE 페이로드.
 *
 * 새 메시지가 저장되면 방 참여자 전원에게 room-update 이벤트로 발송된다.
 * 클라이언트는 이를 받아 방 목록의 마지막 메시지 미리보기/시각/안읽음 수를 갱신한다.
 */
@Schema(description = "채팅방 목록 갱신 이벤트 (SSE room-update)")
public record ChatRoomUpdateResponse(

        @Schema(description = "채팅방 ID", example = "10")
        Long roomId,

        @Schema(description = "메시지 ID", example = "105")
        Long messageId,

        @Schema(description = "발신자 ID", example = "1")
        Long senderId,

        @Schema(description = "메시지 타입", example = "TEXT")
        MessageType type,

        @Schema(description = "목록 미리보기 텍스트", example = "내일 7시에 봬요!")
        String preview,

        @Schema(description = "메시지 생성 시각")
        OffsetDateTime createdAt
) {
}
