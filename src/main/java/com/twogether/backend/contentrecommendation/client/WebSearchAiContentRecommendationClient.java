package com.twogether.backend.contentrecommendation.client;

import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 실제 제공업체 확정 후 구현할 교체 지점이다. 구현 시 관심사, limit, language=ko,
 * country=KR을 전달하고 검색으로 확인된 HTTPS URL만 구조화해 반환해야 한다.
 * API 키와 모델명은 각각 AI_CONTENT_RECOMMENDATION_API_KEY,
 * AI_CONTENT_RECOMMENDATION_MODEL 환경변수에서 주입하도록 구성한다.
 */
@Component
@ConditionalOnProperty(name = "app.ai.content-recommendation.mode", havingValue = "web-search")
public class WebSearchAiContentRecommendationClient implements AiContentRecommendationClient {

    private final String apiKey;
    private final String model;

    public WebSearchAiContentRecommendationClient(
            @Value("${AI_CONTENT_RECOMMENDATION_API_KEY:}") String apiKey,
            @Value("${AI_CONTENT_RECOMMENDATION_MODEL:}") String model
    ) {
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public List<RecommendedContent> recommend(ContentRecommendationContext context) {
        if (apiKey.isBlank() || model.isBlank()) {
            throw new BusinessException(ErrorCode.AI_CLIENT_NOT_CONFIGURED);
        }
        // 제공업체가 확정되기 전에는 설정이 있더라도 임의의 외부 API를 호출하지 않는다.
        throw new BusinessException(ErrorCode.AI_CLIENT_NOT_CONFIGURED);
    }
}
