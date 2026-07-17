package com.twogether.backend.verification.dto.response;

import com.twogether.backend.verification.domain.AiStatus;
import com.twogether.backend.verification.domain.Verification;

import java.time.OffsetDateTime;

public record VerificationResponse(
        Long id,
        Long gatheringId,
        Long uploaderId,
        String photoUrl,
        String reviewText,
        AiStatus aiStatus,
        String aiReason,
        OffsetDateTime createdAt,
        OffsetDateTime verifiedAt
) {
    public static VerificationResponse from(Verification verification) {
        return new VerificationResponse(
                verification.getId(),
                verification.getGathering().getId(),
                verification.getUploader().getId(),
                verification.getPhotoUrl(),
                verification.getReviewText(),
                verification.getAiStatus(),
                verification.getAiReason(),
                verification.getCreatedAt(),
                verification.getVerifiedAt()
        );
    }
}
