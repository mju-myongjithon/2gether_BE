package com.twogether.backend.chat.recommendation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.global.ai.ExternalAiFailureType;
import com.twogether.backend.global.ai.ExternalAiUnavailableException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;

@Component
public class GeminiChatRecommendationGateway {
    private static final String API_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";

    private final ChatRecommendationAiProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    @Autowired
    public GeminiChatRecommendationGateway(ChatRecommendationAiProperties properties, ObjectMapper objectMapper) {
        this(properties, objectMapper, createRestClient(properties));
    }

    GeminiChatRecommendationGateway(ChatRecommendationAiProperties properties, ObjectMapper objectMapper,
                                    RestClient restClient) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = restClient;
    }

    public JsonNode generate(String prompt, Map<String, Object> responseSchema) {
        validateConfiguration();
        final JsonNode response;
        try {
            response = restClient.post()
                    .uri(API_BASE_URL + properties.getModel() + ":generateContent")
                    .header("x-goog-api-key", properties.getApiKey())
                    .body(Map.of(
                            "contents", List.of(Map.of("role", "user", "parts", List.of(Map.of("text", prompt)))),
                            "generationConfig", Map.of(
                                    "temperature", 0.4,
                                    "responseMimeType", "application/json",
                                    "responseSchema", responseSchema)))
                    .retrieve()
                    .body(JsonNode.class);
        } catch (ResourceAccessException exception) {
            if (hasCause(exception, SocketTimeoutException.class)) {
                throw unavailable(ErrorCode.GEMINI_CHAT_RECOMMENDATION_TIMEOUT, ExternalAiFailureType.READ_TIMEOUT);
            }
            throw unavailable(ErrorCode.GEMINI_CHAT_RECOMMENDATION_CALL_FAILED, ExternalAiFailureType.CONNECTION_FAILED);
        } catch (RestClientResponseException exception) {
            int status = exception.getStatusCode().value();
            if (status == 429) throw unavailable(ErrorCode.GEMINI_CHAT_RECOMMENDATION_CALL_FAILED, ExternalAiFailureType.RATE_LIMITED);
            if (status == 500 || status == 502 || status == 503 || status == 504) {
                throw unavailable(ErrorCode.GEMINI_CHAT_RECOMMENDATION_CALL_FAILED, ExternalAiFailureType.SERVER_ERROR);
            }
            throw new BusinessException(ErrorCode.GEMINI_CHAT_RECOMMENDATION_CALL_FAILED);
        } catch (RestClientException exception) {
            throw new BusinessException(ErrorCode.GEMINI_CHAT_RECOMMENDATION_CALL_FAILED);
        }

        try {
            String text = response.path("candidates").path(0).path("content")
                    .path("parts").path(0).path("text").asText(null);
            if (text == null) throw new JsonProcessingException("Gemini response text is missing") { };
            return objectMapper.readTree(text);
        } catch (JsonProcessingException | NullPointerException exception) {
            throw new BusinessException(ErrorCode.GEMINI_CHAT_RECOMMENDATION_RESPONSE_PARSE_FAILED);
        }
    }

    private void validateConfiguration() {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()
                || properties.getModel() == null || properties.getModel().isBlank()
                || !properties.getModel().matches("[A-Za-z0-9._-]+")
                || properties.getConnectTimeout() == null || properties.getConnectTimeout().isNegative()
                || properties.getReadTimeout() == null || properties.getReadTimeout().isNegative()) {
            throw new BusinessException(ErrorCode.GEMINI_CHAT_RECOMMENDATION_NOT_CONFIGURED);
        }
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

    private static RestClient createRestClient(ChatRecommendationAiProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getConnectTimeout());
        factory.setReadTimeout(properties.getReadTimeout());
        return RestClient.builder().requestFactory(factory).build();
    }
}
