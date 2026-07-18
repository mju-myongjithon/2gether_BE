package com.twogether.backend.chat.recommendation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@ConditionalOnExpression("'${app.ai.chat-recommendation.topic-mode:mock}' == 'gemini' or '${app.ai.chat-recommendation.topic-mode:mock}' == 'demo'")
public class GeminiAiTopicRecommendationClient implements AiTopicRecommendationClient {
    static final int MAX_TITLE_LENGTH = 80;
    static final int MAX_CONTENT_LENGTH = 500;

    private final GeminiChatRecommendationGateway gateway;
    private final ObjectMapper objectMapper;

    public GeminiAiTopicRecommendationClient(GeminiChatRecommendationGateway gateway, ObjectMapper objectMapper) {
        this.gateway = gateway;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<RecommendedTopic> recommend(TopicRecommendationContext context) {
        JsonNode result = gateway.generate(createPrompt(context), schema());
        JsonNode topics = result.path("topics");
        if (!topics.isArray() || topics.isEmpty() || topics.size() > 3) throw invalidResult();

        List<RecommendedTopic> values = new ArrayList<>();
        Set<String> titles = new HashSet<>();
        for (JsonNode node : topics) {
            if (!node.hasNonNull("title") || !node.path("title").isTextual()
                    || !node.hasNonNull("content") || !node.path("content").isTextual()) throw invalidResult();
            String title = node.path("title").textValue().trim();
            String content = node.path("content").textValue().trim();
            if (title.isEmpty() || content.isEmpty() || title.length() > MAX_TITLE_LENGTH
                    || content.length() > MAX_CONTENT_LENGTH || !titles.add(title.toLowerCase())) throw invalidResult();
            values.add(new RecommendedTopic(title, content));
        }
        return List.copyOf(values);
    }

    private String createPrompt(TopicRecommendationContext context) {
        try {
            return """
                    당신은 모임 채팅방의 대화를 돕는 진행자입니다. 입력 정보를 바탕으로 대화 주제 1~3개를 한국어로 생성하세요.
                    어색함을 줄이고 자연스럽게 대화가 이어지며 모임 목적과 관심사를 반영해야 합니다.
                    너무 사적이거나 민감한 질문은 금지하고, 실제 채팅방에서 바로 쓸 수 있게 짧고 구체적으로 작성하세요.
                    제목은 80자 이하, 내용은 500자 이하이며 제목을 중복하지 마세요. 입력에 없는 개인정보를 추측하지 마세요.
                    입력 JSON:
                    %s
                    """.formatted(objectMapper.writeValueAsString(context));
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.GEMINI_CHAT_RECOMMENDATION_RESPONSE_PARSE_FAILED);
        }
    }

    private Map<String, Object> schema() {
        return Map.of(
                "type", "OBJECT",
                "properties", Map.of("topics", Map.of(
                        "type", "ARRAY", "minItems", 1, "maxItems", 3,
                        "items", Map.of(
                                "type", "OBJECT",
                                "properties", Map.of(
                                        "title", Map.of("type", "STRING"),
                                        "content", Map.of("type", "STRING")),
                                "required", List.of("title", "content")))),
                "required", List.of("topics"));
    }

    private BusinessException invalidResult() {
        return new BusinessException(ErrorCode.INVALID_GEMINI_TOPIC_RECOMMENDATION_RESULT);
    }
}
