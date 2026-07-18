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
@ConditionalOnExpression("'${app.ai.chat-recommendation.mission-mode:mock}' == 'gemini' or '${app.ai.chat-recommendation.mission-mode:mock}' == 'demo'")
public class GeminiAiMissionRecommendationClient implements AiMissionRecommendationClient {
    static final int MAX_TITLE_LENGTH = 80;
    static final int MAX_CONTENT_LENGTH = 500;

    private final GeminiChatRecommendationGateway gateway;
    private final ObjectMapper objectMapper;

    public GeminiAiMissionRecommendationClient(GeminiChatRecommendationGateway gateway, ObjectMapper objectMapper) {
        this.gateway = gateway;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<RecommendedMission> recommend(MissionRecommendationContext context) {
        JsonNode result = gateway.generate(createPrompt(context), schema());
        JsonNode missions = result.path("missions");
        if (!missions.isArray() || missions.isEmpty() || missions.size() > 3) throw invalidResult();

        List<RecommendedMission> values = new ArrayList<>();
        Set<String> titles = new HashSet<>();
        for (JsonNode node : missions) {
            if (!node.hasNonNull("title") || !node.path("title").isTextual()
                    || !node.hasNonNull("content") || !node.path("content").isTextual()
                    || !node.hasNonNull("difficulty") || !node.path("difficulty").isTextual()) throw invalidResult();
            String title = node.path("title").textValue().trim();
            String content = node.path("content").textValue().trim();
            final MissionDifficulty difficulty;
            try {
                difficulty = MissionDifficulty.valueOf(node.path("difficulty").textValue());
            } catch (IllegalArgumentException exception) {
                throw invalidResult();
            }
            if (title.isEmpty() || content.isEmpty() || title.length() > MAX_TITLE_LENGTH
                    || content.length() > MAX_CONTENT_LENGTH || !titles.add(title.toLowerCase())) throw invalidResult();
            values.add(new RecommendedMission(title, content, difficulty));
        }
        return List.copyOf(values);
    }

    private String createPrompt(MissionRecommendationContext context) {
        try {
            return """
                    당신은 모임 참여를 돕는 활동 진행자입니다. 입력 정보를 바탕으로 함께 수행할 행동형 미션 1~3개를 한국어로 생성하세요.
                    모임 카테고리와 관심사를 반영하고 해당 모임에서 실제 수행 가능해야 합니다.
                    너무 비싸거나 위험하거나 오래 걸리는 활동은 금지합니다. 난이도는 EASY, MEDIUM, HARD 중 하나만 사용하세요.
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
                "properties", Map.of("missions", Map.of(
                        "type", "ARRAY", "minItems", 1, "maxItems", 3,
                        "items", Map.of(
                                "type", "OBJECT",
                                "properties", Map.of(
                                        "title", Map.of("type", "STRING"),
                                        "content", Map.of("type", "STRING"),
                                        "difficulty", Map.of("type", "STRING", "enum", List.of("EASY", "MEDIUM", "HARD"))),
                                "required", List.of("title", "content", "difficulty")))),
                "required", List.of("missions"));
    }

    private BusinessException invalidResult() {
        return new BusinessException(ErrorCode.INVALID_GEMINI_MISSION_RECOMMENDATION_RESULT);
    }
}
