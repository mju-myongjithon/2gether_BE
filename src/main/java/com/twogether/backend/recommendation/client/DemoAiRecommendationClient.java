package com.twogether.backend.recommendation.client;

import com.twogether.backend.global.ai.DemoFallbackExecutor;
import com.twogether.backend.recommendation.dto.request.AiGatheringRecommendationRequest;
import com.twogether.backend.recommendation.dto.response.AiRecommendationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
@ConditionalOnProperty(name = "app.ai.member-recommendation.mode", havingValue = "demo")
public class DemoAiRecommendationClient implements AiRecommendationClient {
    private static final Logger log = LoggerFactory.getLogger(DemoAiRecommendationClient.class);
    private final GeminiAiRecommendationClient gemini;
    private final MockAiRecommendationClient mock;

    public DemoAiRecommendationClient(GeminiAiRecommendationClient gemini, MockAiRecommendationClient mock) {
        this.gemini = gemini;
        this.mock = mock;
    }

    @Override
    public AiRecommendationResponse recommendMembers(AiGatheringRecommendationRequest request) {
        return DemoFallbackExecutor.execute("member-recommendation", log,
                () -> gemini.recommendMembers(request), () -> mock.recommendMembers(request));
    }
}
