package com.twogether.backend.contentrecommendation.client;

import java.util.List;

public interface AiContentRecommendationClient {

    List<RecommendedContent> recommend(ContentRecommendationContext context);
}
