package com.twogether.backend.gatheringapplication.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "모임 신청 거절 요청")
public record GatheringApplicationRejectRequest(

        @Schema(description = "거절 사유", example = "현재 모집 인원이 마감되었습니다.")
        String rejectReason

) {
}
