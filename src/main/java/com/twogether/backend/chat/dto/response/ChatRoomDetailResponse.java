package com.twogether.backend.chat.dto.response;

import com.twogether.backend.chat.domain.ChatRoomType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "채팅방 상세 응답")
public record ChatRoomDetailResponse(

        @Schema(description = "채팅방 ID", example = "10")
        Long roomId,

        @Schema(description = "연결된 모임 ID", example = "1")
        Long gatheringId,

        @Schema(description = "채팅방 유형", example = "GROUP")
        ChatRoomType type,

        @Schema(description = "채팅방 제목", example = "인문X자연 해커톤 팀")
        String title,

        @Schema(description = "채팅방 생성 일시", example = "2026-07-09T20:20:00+09:00")
        OffsetDateTime createdAt,

        @Schema(description = "참여자 목록")
        List<ChatRoomMemberResponse> members

) {
}
