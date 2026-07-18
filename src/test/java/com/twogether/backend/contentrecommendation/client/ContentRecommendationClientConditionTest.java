package com.twogether.backend.contentrecommendation.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twogether.backend.contentrecommendation.config.ContentRecommendationAiProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class ContentRecommendationClientConditionTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withBean(ContentRecommendationAiProperties.class)
            .withBean(ObjectMapper.class)
            .withUserConfiguration(MockAiContentRecommendationClient.class, WebSearchAiContentRecommendationClient.class,
                    DemoAiContentRecommendationClient.class);

    @Test void defaultsToMock() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(AiContentRecommendationClient.class);
            assertThat(context).hasSingleBean(MockAiContentRecommendationClient.class);
        });
    }

    @Test void switchesToGemini() {
        runner.withPropertyValues("app.ai.content-recommendation.mode=gemini").run(context -> {
            assertThat(context).hasSingleBean(AiContentRecommendationClient.class);
            assertThat(context).hasSingleBean(WebSearchAiContentRecommendationClient.class);
        });
    }

    @Test void switchesToDemoFallback() {
        runner.withPropertyValues("app.ai.content-recommendation.mode=demo").run(context ->
                assertThat(context.getBean(AiContentRecommendationClient.class))
                        .isInstanceOf(DemoAiContentRecommendationClient.class));
    }
}
