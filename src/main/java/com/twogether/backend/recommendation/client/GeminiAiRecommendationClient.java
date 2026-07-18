package com.twogether.backend.recommendation.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.global.ai.ExternalAiFailureType;
import com.twogether.backend.global.ai.ExternalAiUnavailableException;
import com.twogether.backend.recommendation.config.MemberRecommendationAiProperties;
import com.twogether.backend.recommendation.dto.request.AiGatheringRecommendationRequest;
import com.twogether.backend.recommendation.dto.response.AiRecommendationResponse;
import com.twogether.backend.recommendation.dto.response.AiRecommendedCandidate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@ConditionalOnExpression("'${app.ai.member-recommendation.mode:mock}' == 'gemini' or '${app.ai.member-recommendation.mode:mock}' == 'demo'")
public class GeminiAiRecommendationClient implements AiRecommendationClient {

    private static final String API_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";

    private final MemberRecommendationAiProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    @Autowired
    public GeminiAiRecommendationClient(MemberRecommendationAiProperties properties, ObjectMapper objectMapper) {
        this(properties, objectMapper, createRestClient(properties));
    }

    GeminiAiRecommendationClient(MemberRecommendationAiProperties properties, ObjectMapper objectMapper, RestClient restClient) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = restClient;
    }

    @Override
    public AiRecommendationResponse recommendMembers(AiGatheringRecommendationRequest request) {
        validateConfiguration();
        JsonNode response;
        try {
            response = restClient.post()
                    .uri(API_BASE_URL + properties.getModel() + ":generateContent")
                    .header("x-goog-api-key", properties.getApiKey())
                    .body(createRequestBody(request))
                    .retrieve()
                    .body(JsonNode.class);
        } catch (ResourceAccessException exception) {
            if (hasCause(exception, SocketTimeoutException.class)) {
                throw unavailable(ErrorCode.GEMINI_RECOMMENDATION_TIMEOUT, timeoutType(exception));
            }
            throw unavailable(ErrorCode.GEMINI_RECOMMENDATION_CALL_FAILED, ExternalAiFailureType.CONNECTION_FAILED);
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 429) {
                throw unavailable(ErrorCode.GEMINI_RECOMMENDATION_CALL_FAILED, ExternalAiFailureType.RATE_LIMITED);
            }
            if (isFallbackServerStatus(exception.getStatusCode().value())) {
                throw unavailable(ErrorCode.GEMINI_RECOMMENDATION_CALL_FAILED, ExternalAiFailureType.SERVER_ERROR);
            }
            throw new BusinessException(ErrorCode.GEMINI_RECOMMENDATION_CALL_FAILED);
        } catch (RestClientException exception) {
            throw new BusinessException(ErrorCode.GEMINI_RECOMMENDATION_CALL_FAILED);
        }
        return parseAndValidate(response, request);
    }

    private Map<String, Object> createRequestBody(AiGatheringRecommendationRequest request) {
        String prompt;
        try {
            prompt = "다음 모임에 적합한 팀원을 후보 목록 안에서만 " + request.recommendationCount()
                    + "명 추천하세요. 점수는 0~100 정수이며 추천 이유는 한국어로 작성하세요.\n입력 JSON:\n"
                    + objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.GEMINI_RECOMMENDATION_RESPONSE_PARSE_FAILED);
        }
        Map<String, Object> schema = Map.of(
                "type", "OBJECT",
                "properties", Map.of("recommendations", Map.of(
                        "type", "ARRAY",
                        "items", Map.of(
                                "type", "OBJECT",
                                "properties", Map.of(
                                        "userId", Map.of("type", "INTEGER"),
                                        "score", Map.of("type", "INTEGER"),
                                        "reason", Map.of("type", "STRING")
                                ),
                                "required", List.of("userId", "score", "reason")
                        )
                )),
                "required", List.of("recommendations")
        );
        return Map.of(
                "contents", List.of(Map.of("role", "user", "parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of("responseMimeType", "application/json", "responseSchema", schema)
        );
    }

    private AiRecommendationResponse parseAndValidate(JsonNode response, AiGatheringRecommendationRequest request) {
        final JsonNode result;
        try {
            String text = response.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText(null);
            if (text == null) {
                throw new JsonProcessingException("Gemini response text is missing") { };
            }
            result = objectMapper.readTree(text);
        } catch (JsonProcessingException | NullPointerException exception) {
            throw new BusinessException(ErrorCode.GEMINI_RECOMMENDATION_RESPONSE_PARSE_FAILED);
        }

        JsonNode recommendations = result.path("recommendations");
        if (!recommendations.isArray() || recommendations.size() > request.recommendationCount()) {
            throw invalidResult();
        }
        Set<Long> candidateIds = new HashSet<>();
        request.candidates().forEach(candidate -> candidateIds.add(candidate.userId()));
        Set<Long> returnedIds = new HashSet<>();
        List<AiRecommendedCandidate> values = new ArrayList<>();
        for (JsonNode node : recommendations) {
            if (!node.hasNonNull("userId") || !node.path("userId").canConvertToLong()
                    || !node.hasNonNull("score") || !node.path("score").isIntegralNumber()
                    || !node.hasNonNull("reason") || !node.path("reason").isTextual()) {
                throw invalidResult();
            }
            long userId = node.path("userId").longValue();
            int score = node.path("score").intValue();
            String reason = node.path("reason").textValue();
            if (!candidateIds.contains(userId) || !returnedIds.add(userId)
                    || score < 0 || score > 100 || reason.isBlank()) {
                throw invalidResult();
            }
            values.add(new AiRecommendedCandidate(userId, score, reason));
        }
        return new AiRecommendationResponse(values);
    }

    private void validateConfiguration() {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()
                || properties.getModel() == null || properties.getModel().isBlank()
                || !properties.getModel().matches("[A-Za-z0-9._-]+")) {
            throw new BusinessException(ErrorCode.GEMINI_RECOMMENDATION_NOT_CONFIGURED);
        }
    }

    private BusinessException invalidResult() {
        return new BusinessException(ErrorCode.INVALID_GEMINI_RECOMMENDATION_RESULT);
    }

    private static boolean hasCause(Throwable throwable, Class<? extends Throwable> type) {
        for (Throwable current = throwable; current != null; current = current.getCause()) {
            if (type.isInstance(current)) return true;
        }
        return false;
    }

    private ExternalAiUnavailableException unavailable(ErrorCode code, ExternalAiFailureType type) {
        return new ExternalAiUnavailableException(code, type);
    }

    private static ExternalAiFailureType timeoutType(Throwable throwable) {
        return hasCause(throwable, java.net.ConnectException.class)
                ? ExternalAiFailureType.CONNECT_TIMEOUT : ExternalAiFailureType.READ_TIMEOUT;
    }

    private static boolean isFallbackServerStatus(int status) {
        return status == 500 || status == 502 || status == 503 || status == 504;
    }

    private static RestClient createRestClient(MemberRecommendationAiProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getConnectTimeout());
        factory.setReadTimeout(properties.getReadTimeout());
        return RestClient.builder().requestFactory(factory).build();
    }
}
