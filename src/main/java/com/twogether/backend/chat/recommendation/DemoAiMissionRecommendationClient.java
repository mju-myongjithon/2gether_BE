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
@ConditionalOnProperty(name = "app.ai.chat-recommendation.mission-mode", havingValue = "demo")
public class DemoAiMissionRecommendationClient implements AiMissionRecommendationClient {
    private static final Logger log = LoggerFactory.getLogger(DemoAiMissionRecommendationClient.class);
    private final GeminiAiMissionRecommendationClient gemini;
    private final MockAiMissionRecommendationClient mock;

    public DemoAiMissionRecommendationClient(GeminiAiMissionRecommendationClient gemini, MockAiMissionRecommendationClient mock) {
        this.gemini = gemini;
        this.mock = mock;
    }

    @Override
    public List<RecommendedMission> recommend(MissionRecommendationContext context) {
        return DemoFallbackExecutor.execute("gathering-mission-recommendation", log,
                () -> gemini.recommend(context), () -> mock.recommend(context));
    }
}
