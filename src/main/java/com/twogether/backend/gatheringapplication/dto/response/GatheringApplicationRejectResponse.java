package com.twogether.backend.gatheringapplication.dto.response;

import com.twogether.backend.gatheringapplication.domain.ApplicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "모임 신청 거절 응답")
public record GatheringApplicationRejectResponse(

        @Schema(description = "신청 ID", example = "1")
        Long applicationId,

        @Schema(description = "신청 상태", example = "REJECTED")
        ApplicationStatus status,

        @Schema(description = "거절 사유", example = "현재 모집 인원이 마감되었습니다.")
        String rejectReason,

        @Schema(description = "심사 일시", example = "2026-07-09T19:55:00+09:00")
        OffsetDateTime reviewedAt

) {
}
