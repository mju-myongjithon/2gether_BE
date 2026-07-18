package com.twogether.backend.verification.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twogether.backend.verification.config.VerificationAiProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class VerificationClientConditionTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withBean(VerificationAiProperties.class).withBean(ObjectMapper.class)
            .withUserConfiguration(MockAiVerificationClient.class, GeminiAiVerificationClient.class,
                    SecureVerificationImageDownloader.class, DemoAiVerificationClient.class);

    @Test void usesMockByDefault() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(AiVerificationClient.class);
            assertThat(context).hasSingleBean(MockAiVerificationClient.class);
        });
    }
    @Test void usesGeminiWhenConfigured() {
        runner.withPropertyValues("app.ai.verification.mode=gemini").run(context -> {
            assertThat(context).hasSingleBean(AiVerificationClient.class);
            assertThat(context).hasSingleBean(GeminiAiVerificationClient.class);
            assertThat(context).hasSingleBean(SecureVerificationImageDownloader.class);
        });
    }
    @Test void usesDemoFallbackWhenConfigured() {
        runner.withPropertyValues("app.ai.verification.mode=demo").run(context ->
                assertThat(context.getBean(AiVerificationClient.class)).isInstanceOf(DemoAiVerificationClient.class));
    }
}
