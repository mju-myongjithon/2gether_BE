package com.twogether.backend.gathering.dto.response;

import com.twogether.backend.gathering.domain.GatheringStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "모임 확정 응답")
public record GatheringConfirmResponse(

        @Schema(description = "모임 ID", example = "1")
        Long gatheringId,

        @Schema(description = "모임 상태", example = "CONFIRMED")
        GatheringStatus status,

        @Schema(description = "확정 일시", example = "2026-07-09T20:20:00+09:00")
        OffsetDateTime confirmedAt,

        @Schema(description = "자동 생성된 그룹 채팅방 ID", example = "10")
        Long chatRoomId

) {
}
