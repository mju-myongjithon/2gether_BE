package com.twogether.backend.verification.client;

import com.twogether.backend.global.ai.DemoFallbackExecutor;
import com.twogether.backend.verification.dto.ai.AiVerificationRequest;
import com.twogether.backend.verification.dto.ai.AiVerificationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
@ConditionalOnProperty(name = "app.ai.verification.mode", havingValue = "demo")
public class DemoAiVerificationClient implements AiVerificationClient {
    private static final Logger log = LoggerFactory.getLogger(DemoAiVerificationClient.class);
    private final GeminiAiVerificationClient gemini;
    private final MockAiVerificationClient mock;

    public DemoAiVerificationClient(GeminiAiVerificationClient gemini, MockAiVerificationClient mock) {
        this.gemini = gemini;
        this.mock = mock;
    }

    @Override
    public AiVerificationResult verify(AiVerificationRequest request) {
        return DemoFallbackExecutor.execute("activity-verification", log,
                () -> gemini.verify(request), () -> mock.verify(request));
    }
}
