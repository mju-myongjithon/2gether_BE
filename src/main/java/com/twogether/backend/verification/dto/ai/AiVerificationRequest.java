package com.twogether.backend.verification.dto.ai;

import com.twogether.backend.gathering.domain.GatheringCategory;
import java.time.OffsetDateTime;

public record AiVerificationRequest(
        Long verificationId,
        Long gatheringId,
        String gatheringTitle,
        String gatheringContent,
        GatheringCategory gatheringCategory,
        String gatheringLocation,
        OffsetDateTime meetAt,
        String photoUrl,
        String reviewText
) {
}
