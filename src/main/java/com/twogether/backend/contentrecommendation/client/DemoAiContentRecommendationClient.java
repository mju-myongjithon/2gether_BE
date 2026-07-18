package com.twogether.backend.contentrecommendation.client;

import com.twogether.backend.global.ai.DemoFallbackExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

@Primary
@Component
@ConditionalOnProperty(name = "app.ai.content-recommendation.mode", havingValue = "demo")
public class DemoAiContentRecommendationClient implements AiContentRecommendationClient {
    private static final Logger log = LoggerFactory.getLogger(DemoAiContentRecommendationClient.class);
    private final WebSearchAiContentRecommendationClient gemini;
    private final MockAiContentRecommendationClient mock;

    public DemoAiContentRecommendationClient(WebSearchAiContentRecommendationClient gemini,
                                             MockAiContentRecommendationClient mock) {
        this.gemini = gemini;
        this.mock = mock;
    }

    @Override
    public List<RecommendedContent> recommend(ContentRecommendationContext context) {
        return DemoFallbackExecutor.execute("personalized-content-recommendation", log,
                () -> gemini.recommend(context), () -> mock.recommend(context));
    }
}
