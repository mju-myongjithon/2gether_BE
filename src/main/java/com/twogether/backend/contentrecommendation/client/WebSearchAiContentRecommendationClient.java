package com.twogether.backend.contentrecommendation.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twogether.backend.contentrecommendation.config.ContentRecommendationAiProperties;
import com.twogether.backend.contentrecommendation.domain.ContentType;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.global.ai.ExternalAiFailureType;
import com.twogether.backend.global.ai.ExternalAiUnavailableException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
@ConditionalOnExpression("'${app.ai.content-recommendation.mode:mock}' == 'gemini' or '${app.ai.content-recommendation.mode:mock}' == 'demo'")
public class WebSearchAiContentRecommendationClient implements AiContentRecommendationClient {
    private static final String API_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final int MAX_TITLE_LENGTH = 120;
    private static final int MAX_DESCRIPTION_LENGTH = 500;

    private final ContentRecommendationAiProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    @Autowired
    public WebSearchAiContentRecommendationClient(ContentRecommendationAiProperties properties,
                                                  ObjectMapper objectMapper) {
        this(properties, objectMapper, createRestClient(properties));
    }

    WebSearchAiContentRecommendationClient(ContentRecommendationAiProperties properties,
                                           ObjectMapper objectMapper, RestClient restClient) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = restClient;
    }

    @Override
    public List<RecommendedContent> recommend(ContentRecommendationContext context) {
        validateConfiguration();
        JsonNode response;
        try {
            response = restClient.post()
                    .uri(API_BASE_URL + properties.getModel() + ":generateContent")
                    .header("x-goog-api-key", properties.getApiKey())
                    .body(createRequestBody(context))
                    .retrieve().body(JsonNode.class);
        } catch (ResourceAccessException exception) {
            if (hasCause(exception, SocketTimeoutException.class)) {
                throw unavailable(ErrorCode.GEMINI_CONTENT_RECOMMENDATION_TIMEOUT, ExternalAiFailureType.READ_TIMEOUT);
            }
            throw unavailable(ErrorCode.GEMINI_CONTENT_RECOMMENDATION_CALL_FAILED, ExternalAiFailureType.CONNECTION_FAILED);
        } catch (RestClientResponseException exception) {
            int status = exception.getStatusCode().value();
            if (status == 429) throw unavailable(ErrorCode.GEMINI_CONTENT_RECOMMENDATION_CALL_FAILED, ExternalAiFailureType.RATE_LIMITED);
            if (status == 500 || status == 502 || status == 503 || status == 504) {
                throw unavailable(ErrorCode.GEMINI_CONTENT_RECOMMENDATION_CALL_FAILED, ExternalAiFailureType.SERVER_ERROR);
            }
            throw new BusinessException(ErrorCode.GEMINI_CONTENT_RECOMMENDATION_CALL_FAILED);
        } catch (RestClientException exception) {
            throw new BusinessException(ErrorCode.GEMINI_CONTENT_RECOMMENDATION_CALL_FAILED);
        }
        return parseAndValidate(response, context);
    }

    private Map<String, Object> createRequestBody(ContentRecommendationContext context) {
        String prompt = "Google 검색을 사용해 다음 관심사와 직접 관련된 최신 한국어 웹 콘텐츠를 최대 "
                + context.limit() + "개 추천하세요. 관심사: " + context.interestTags() + "\n"
                + "실제 검색 근거가 있는 HTTPS URL만 사용하고 광고, 도박, 성인, 불법, 위험 콘텐츠와 "
                + "종료된 행사, 로그인 또는 결제가 필수인 페이지는 제외하세요. 지역 정보가 없으므로 장소를 억지로 추천하지 마세요. "
                + "matchedTags에는 입력 관심사 중 실제 관련 태그만 그대로 넣으세요. "
                + "contentType은 BLOG, ARTICLE, EVENT, PLACE, ACTIVITY 중 하나여야 합니다. "
                + "설명이나 마크다운 코드 펜스 없이 다음 형식의 JSON 객체만 반환하세요: "
                + "{\"recommendations\":[{\"title\":\"...\",\"description\":\"...\",\"url\":\"https://...\","
                + "\"contentType\":\"BLOG\",\"matchedTags\":[\"...\"]}]}";
        return Map.of(
                "contents", List.of(Map.of("role", "user", "parts", List.of(Map.of("text", prompt)))),
                "tools", List.of(Map.of("google_search", Map.of())));
    }

    private List<RecommendedContent> parseAndValidate(JsonNode response, ContentRecommendationContext context) {
        try {
            JsonNode candidate = response.path("candidates").path(0);
            String text = candidate.path("content").path("parts").path(0).path("text").asText(null);
            if (text == null) throw new JsonProcessingException("Gemini response text is missing") { };
            JsonNode root = objectMapper.readTree(stripJsonCodeFence(text));
            JsonNode nodes = root.path("recommendations");
            if (!nodes.isArray() || nodes.size() > context.limit()) throw invalidResult();

            Set<String> groundedUrls = groundingUrls(candidate);
            Set<String> seenUrls = new HashSet<>();
            Set<String> interests = new HashSet<>();
            context.interestTags().forEach(tag -> interests.add(tag.toLowerCase(Locale.ROOT)));
            List<RecommendedContent> result = new ArrayList<>();
            for (JsonNode node : nodes) {
                String title = requiredText(node, "title");
                String description = requiredText(node, "description");
                String url = requiredText(node, "url");
                String type = requiredText(node, "contentType");
                if (title.length() > MAX_TITLE_LENGTH || description.length() > MAX_DESCRIPTION_LENGTH
                        || !groundedUrls.contains(url) || !seenUrls.add(url)) throw invalidResult();
                ContentType contentType;
                try { contentType = ContentType.valueOf(type); }
                catch (IllegalArgumentException exception) { throw invalidResult(); }
                JsonNode tagsNode = node.path("matchedTags");
                if (!tagsNode.isArray()) throw invalidResult();
                List<String> tags = new ArrayList<>();
                for (JsonNode tag : tagsNode) {
                    if (!tag.isTextual() || !interests.contains(tag.textValue().toLowerCase(Locale.ROOT))) throw invalidResult();
                    tags.add(tag.textValue());
                }
                result.add(new RecommendedContent(title, description, url, contentType, List.copyOf(tags)));
            }
            return List.copyOf(result);
        } catch (JsonProcessingException | NullPointerException exception) {
            throw new BusinessException(ErrorCode.GEMINI_CONTENT_RECOMMENDATION_RESPONSE_PARSE_FAILED);
        }
    }

    private Set<String> groundingUrls(JsonNode candidate) {
        Set<String> urls = new LinkedHashSet<>();
        for (JsonNode chunk : candidate.path("groundingMetadata").path("groundingChunks")) {
            String uri = chunk.path("web").path("uri").asText(null);
            if (uri != null) urls.add(uri);
        }
        return urls;
    }

    private String requiredText(JsonNode node, String field) {
        if (!node.path(field).isTextual() || node.path(field).textValue().isBlank()) throw invalidResult();
        return node.path(field).textValue().trim();
    }

    private String stripJsonCodeFence(String text) {
        String trimmed = text.trim();
        if (!trimmed.startsWith("```") || !trimmed.endsWith("```")) return trimmed;
        int firstLineEnd = trimmed.indexOf('\n');
        if (firstLineEnd < 0) return trimmed;
        return trimmed.substring(firstLineEnd + 1, trimmed.length() - 3).trim();
    }

    private void validateConfiguration() {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()
                || properties.getModel() == null || !properties.getModel().matches("[A-Za-z0-9._-]+")) {
            throw new BusinessException(ErrorCode.GEMINI_CONTENT_RECOMMENDATION_NOT_CONFIGURED);
        }
    }

    private BusinessException invalidResult() {
        return new BusinessException(ErrorCode.INVALID_GEMINI_CONTENT_RECOMMENDATION_RESULT);
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

    private static RestClient createRestClient(ContentRecommendationAiProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getConnectTimeout());
        factory.setReadTimeout(properties.getReadTimeout());
        return RestClient.builder().requestFactory(factory).build();
    }
}
