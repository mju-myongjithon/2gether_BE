package com.twogether.backend.chat.recommendation;

import java.util.List;

public interface AiTopicRecommendationClient {
    List<RecommendedTopic> recommend(TopicRecommendationContext context);
}
