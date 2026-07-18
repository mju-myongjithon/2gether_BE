package com.twogether.backend.chat.recommendation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class ChatRecommendationClientConditionTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withBean(ChatRecommendationAiProperties.class)
            .withBean(ObjectMapper.class)
            .withUserConfiguration(GeminiChatRecommendationGateway.class,
                    MockAiTopicRecommendationClient.class, GeminiAiTopicRecommendationClient.class,
                    MockAiMissionRecommendationClient.class, GeminiAiMissionRecommendationClient.class,
                    DemoAiTopicRecommendationClient.class, DemoAiMissionRecommendationClient.class);

    @Test void usesMocksByDefault() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(AiTopicRecommendationClient.class);
            assertThat(context).hasSingleBean(MockAiTopicRecommendationClient.class);
            assertThat(context).hasSingleBean(AiMissionRecommendationClient.class);
            assertThat(context).hasSingleBean(MockAiMissionRecommendationClient.class);
        });
    }

    @Test void switchesTopicToGemini() {
        runner.withPropertyValues("app.ai.chat-recommendation.topic-mode=gemini").run(context -> {
            assertThat(context).hasSingleBean(AiTopicRecommendationClient.class);
            assertThat(context).hasSingleBean(GeminiAiTopicRecommendationClient.class);
            assertThat(context).hasSingleBean(MockAiMissionRecommendationClient.class);
        });
    }

    @Test void switchesMissionToGemini() {
        runner.withPropertyValues("app.ai.chat-recommendation.mission-mode=gemini").run(context -> {
            assertThat(context).hasSingleBean(AiMissionRecommendationClient.class);
            assertThat(context).hasSingleBean(GeminiAiMissionRecommendationClient.class);
            assertThat(context).hasSingleBean(MockAiTopicRecommendationClient.class);
        });
    }

    @Test void supportsDemoModeIndependentlyForEachFeature() {
        runner.withPropertyValues("app.ai.chat-recommendation.topic-mode=demo",
                "app.ai.chat-recommendation.mission-mode=demo").run(context -> {
            assertThat(context.getBean(AiTopicRecommendationClient.class)).isInstanceOf(DemoAiTopicRecommendationClient.class);
            assertThat(context.getBean(AiMissionRecommendationClient.class)).isInstanceOf(DemoAiMissionRecommendationClient.class);
        });
    }
}
