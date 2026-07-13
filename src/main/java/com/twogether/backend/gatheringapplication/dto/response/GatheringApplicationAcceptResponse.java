package com.twogether.backend.gatheringapplication.dto.response;

import com.twogether.backend.gatheringapplication.domain.ApplicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "모임 신청 수락 응답")
public record GatheringApplicationAcceptResponse(

        @Schema(description = "신청 ID", example = "1")
        Long applicationId,

        @Schema(description = "신청 상태", example = "ACCEPTED")
        ApplicationStatus status,

        @Schema(description = "생성된 모임 멤버 ID", example = "5")
        Long memberId,

        @Schema(description = "심사 일시", example = "2026-07-09T19:50:00+09:00")
        OffsetDateTime reviewedAt

) {
}
