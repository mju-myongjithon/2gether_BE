package com.twogether.backend.chat.recommendation;

import java.util.List;

public interface AiMissionRecommendationClient {
    List<RecommendedMission> recommend(MissionRecommendationContext context);
}
