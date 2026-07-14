package com.twogether.backend.chat.dto.response;

import com.twogether.backend.chat.domain.ChatRoomType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "채팅방 목록 항목 응답")
public record ChatRoomSummaryResponse(

        @Schema(description = "채팅방 ID", example = "10")
        Long roomId,

        @Schema(description = "연결된 모임 ID", example = "1")
        Long gatheringId,

        @Schema(description = "채팅방 유형", example = "GROUP")
        ChatRoomType type,

        @Schema(description = "채팅방 제목", example = "인문X자연 해커톤 팀")
        String title,

        @Schema(description = "참여 인원 수", example = "4")
        int memberCount,

        @Schema(description = "마지막 메시지 내용", example = "내일 7시에 봬요!")
        String lastMessage,

        @Schema(description = "마지막 메시지 일시", example = "2026-07-13T21:10:00+09:00")
        OffsetDateTime lastMessageAt,

        @Schema(description = "읽지 않은 메시지 수", example = "2")
        int unreadCount

) {
}
