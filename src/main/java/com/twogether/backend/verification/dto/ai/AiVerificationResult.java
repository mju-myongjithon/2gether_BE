package com.twogether.backend.verification.dto.ai;

import com.twogether.backend.verification.domain.AiStatus;

public record AiVerificationResult(AiStatus status, String reason) {
}
