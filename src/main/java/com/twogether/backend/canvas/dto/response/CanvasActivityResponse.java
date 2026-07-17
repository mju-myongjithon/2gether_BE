package com.twogether.backend.canvas.dto.response;

import com.twogether.backend.verification.domain.Verification;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(description = "승인된 활동 상세")
public record CanvasActivityResponse(
        @Schema(example = "2") Long verificationId,
        @Schema(example = "3") Long uploaderId,
        @Schema(example = "1") Long gatheringId,
        @Schema(example = "Campus Canvas 테스트 모임") String gatheringTitle,
        @Schema(example = "모임원들과 함께 교류 활동을 진행했습니다.") String reviewText,
        @Schema(example = "https://example.com/activity-photo.jpg") String photoUrl,
        @Schema(example = "2026-07-17T16:41:31+09:00") OffsetDateTime verifiedAt
) {
    public static CanvasActivityResponse from(Verification verification) {
        return new CanvasActivityResponse(verification.getId(), verification.getUploader().getId(),
                verification.getGathering().getId(), verification.getGathering().getTitle(),
                verification.getReviewText(), verification.getPhotoUrl(), verification.getVerifiedAt());
    }
}
