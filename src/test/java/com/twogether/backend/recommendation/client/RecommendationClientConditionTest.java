package com.twogether.backend.recommendation.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twogether.backend.recommendation.config.MemberRecommendationAiProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class RecommendationClientConditionTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withBean(MemberRecommendationAiProperties.class)
            .withBean(ObjectMapper.class)
            .withUserConfiguration(MockAiRecommendationClient.class, GeminiAiRecommendationClient.class,
                    DemoAiRecommendationClient.class);

    @Test
    void usesMockWhenModeIsMissing() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(AiRecommendationClient.class);
            assertThat(context).hasSingleBean(MockAiRecommendationClient.class);
        });
    }

    @Test
    void usesGeminiWhenModeIsGemini() {
        contextRunner.withPropertyValues("app.ai.member-recommendation.mode=gemini")
                .run(context -> {
                    assertThat(context).hasSingleBean(AiRecommendationClient.class);
                    assertThat(context).hasSingleBean(GeminiAiRecommendationClient.class);
                });
    }

    @Test
    void usesDemoFallbackWhenModeIsDemo() {
        contextRunner.withPropertyValues("app.ai.member-recommendation.mode=demo")
                .run(context -> assertThat(context.getBean(AiRecommendationClient.class))
                        .isInstanceOf(DemoAiRecommendationClient.class));
    }
}
