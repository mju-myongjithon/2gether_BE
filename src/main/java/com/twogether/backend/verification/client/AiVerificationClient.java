package com.twogether.backend.verification.client;

import com.twogether.backend.verification.dto.ai.AiVerificationRequest;
import com.twogether.backend.verification.dto.ai.AiVerificationResult;

public interface AiVerificationClient {
    AiVerificationResult verify(AiVerificationRequest request);
}
