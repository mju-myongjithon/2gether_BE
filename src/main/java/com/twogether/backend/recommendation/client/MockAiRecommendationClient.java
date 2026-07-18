package com.twogether.backend.recommendation.client;

import com.twogether.backend.recommendation.dto.request.AiCandidateInfo;
import com.twogether.backend.recommendation.dto.request.AiGatheringRecommendationRequest;
import com.twogether.backend.recommendation.dto.response.AiRecommendationResponse;
import com.twogether.backend.recommendation.dto.response.AiRecommendedCandidate;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;

import java.util.Comparator;
import java.util.List;

@Component
@ConditionalOnExpression("'${app.ai.member-recommendation.mode:mock}' == 'mock' or '${app.ai.member-recommendation.mode:mock}' == 'demo'")
public class MockAiRecommendationClient implements AiRecommendationClient {

    @Override
    public AiRecommendationResponse recommendMembers(
            AiGatheringRecommendationRequest request
    ) {
        List<AiRecommendedCandidate> recommendations =
                request.candidates().stream()
                        .sorted(
                                Comparator.comparingInt(
                                        AiCandidateInfo::exactMatchCount
                                ).reversed()
                        )
                        .limit(request.recommendationCount())
                        .map(candidate ->
                                new AiRecommendedCandidate(
                                        candidate.userId(),
                                        calculateScore(candidate.exactMatchCount()),
                                        createReason(candidate)
                                )
                        )
                        .toList();

        return new AiRecommendationResponse(recommendations);
    }

    private int calculateScore(int exactMatchCount) {
        return Math.min(100, 60 + exactMatchCount * 10);
    }

    private String createReason(AiCandidateInfo candidate) {
        if (candidate.exactMatchCount() > 0) {
            return "모임 태그와 "
                    + candidate.exactMatchCount()
                    + "개의 태그가 일치합니다.";
        }

        return "모임 정보와 사용자 프로필을 기준으로 추천된 후보입니다.";
    }
}
