package com.twogether.backend.chat.recommendation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twogether.backend.gathering.domain.GatheringCategory;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GeminiAiTopicRecommendationClientTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final GeminiChatRecommendationGateway gateway = mock(GeminiChatRecommendationGateway.class);
    private final GeminiAiTopicRecommendationClient client = new GeminiAiTopicRecommendationClient(gateway, mapper);

    @Test void parsesNormalResponse() {
        respond("{\"topics\":[{\"title\":\"경험 공유\",\"content\":\"최근 경험을 이야기해보세요.\"}]}");
        assertThat(client.recommend(context())).containsExactly(
                new RecommendedTopic("경험 공유", "최근 경험을 이야기해보세요."));
    }

    @Test void rejectsEmptyOrTooManyTopics() {
        assertInvalid("{\"topics\":[]}");
        assertInvalid("{\"topics\":[" + topic("1") + "," + topic("2") + "," + topic("3") + "," + topic("4") + "]}");
    }

    @Test void rejectsBlankFields() {
        assertInvalid("{\"topics\":[{\"title\":\" \",\"content\":\"내용\"}]}");
        assertInvalid("{\"topics\":[{\"title\":\"제목\",\"content\":\" \"}]}");
    }

    @Test void rejectsDuplicateTitles() {
        assertInvalid("{\"topics\":[" + topic("같은 제목") + "," + topic("같은 제목") + "]}");
    }

    private void assertInvalid(String json) {
        respond(json);
        assertThatThrownBy(() -> client.recommend(context()))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INVALID_GEMINI_TOPIC_RECOMMENDATION_RESULT));
    }
    private void respond(String json) { try { when(gateway.generate(anyString(), any())).thenReturn(mapper.readTree(json)); }
        catch (Exception e) { throw new AssertionError(e); } }
    private String topic(String title) { return "{\"title\":\"" + title + "\",\"content\":\"내용\"}"; }
    private TopicRecommendationContext context() { return new TopicRecommendationContext(
            1L, 2L, "모임", "소개", GatheringCategory.STUDY, List.of("개발"), List.of("Java")); }
}
