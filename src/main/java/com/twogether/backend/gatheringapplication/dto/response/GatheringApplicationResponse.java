package com.twogether.backend.gatheringapplication.dto.response;

import com.twogether.backend.gatheringapplication.domain.ApplicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "모임 신청자 정보 응답")
public record GatheringApplicationResponse(

        @Schema(description = "신청 ID", example = "1")
        Long applicationId,

        @Schema(description = "신청 상태", example = "PENDING")
        ApplicationStatus status,

        @Schema(description = "신청 메시지", example = "협업 경험을 쌓고 싶습니다.")
        String message,

        @Schema(description = "신청 일시", example = "2026-07-09T19:30:00+09:00")
        OffsetDateTime appliedAt,

        @Schema(description = "신청자 정보")
        ApplicantResponse applicant

) {
}
