package com.twogether.backend.recommendation.client;

import com.twogether.backend.recommendation.dto.request.AiGatheringRecommendationRequest;
import com.twogether.backend.recommendation.dto.response.AiRecommendationResponse;

public interface AiRecommendationClient {

    AiRecommendationResponse recommendMembers(
            AiGatheringRecommendationRequest request
    );
}