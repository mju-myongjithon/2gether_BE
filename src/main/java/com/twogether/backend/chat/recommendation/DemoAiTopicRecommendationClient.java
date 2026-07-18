package com.twogether.backend.chat.recommendation;

import com.twogether.backend.global.ai.DemoFallbackExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

@Primary
@Component
@ConditionalOnProperty(name = "app.ai.chat-recommendation.topic-mode", havingValue = "demo")
public class DemoAiTopicRecommendationClient implements AiTopicRecommendationClient {
    private static final Logger log = LoggerFactory.getLogger(DemoAiTopicRecommendationClient.class);
    private final GeminiAiTopicRecommendationClient gemini;
    private final MockAiTopicRecommendationClient mock;

    public DemoAiTopicRecommendationClient(GeminiAiTopicRecommendationClient gemini, MockAiTopicRecommendationClient mock) {
        this.gemini = gemini;
        this.mock = mock;
    }

    @Override
    public List<RecommendedTopic> recommend(TopicRecommendationContext context) {
        return DemoFallbackExecutor.execute("conversation-topic-recommendation", log,
                () -> gemini.recommend(context), () -> mock.recommend(context));
    }
}
