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

class GeminiAiMissionRecommendationClientTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final GeminiChatRecommendationGateway gateway = mock(GeminiChatRecommendationGateway.class);
    private final GeminiAiMissionRecommendationClient client = new GeminiAiMissionRecommendationClient(gateway, mapper);

    @Test void parsesNormalResponse() {
        respond("{\"missions\":[{\"title\":\"아이디어 스케치\",\"content\":\"아이디어를 함께 정해보세요.\",\"difficulty\":\"MEDIUM\"}]}");
        assertThat(client.recommend(context())).containsExactly(
                new RecommendedMission("아이디어 스케치", "아이디어를 함께 정해보세요.", MissionDifficulty.MEDIUM));
    }

    @Test void rejectsInvalidDifficulty() {
        assertInvalid("{\"missions\":[" + mission("미션", "NORMAL") + "]}");
    }

    @Test void rejectsBlankFields() {
        assertInvalid("{\"missions\":[{\"title\":\" \",\"content\":\"내용\",\"difficulty\":\"EASY\"}]}");
        assertInvalid("{\"missions\":[{\"title\":\"제목\",\"content\":\" \",\"difficulty\":\"EASY\"}]}");
    }

    @Test void rejectsEmptyOrTooManyMissions() {
        assertInvalid("{\"missions\":[]}");
        assertInvalid("{\"missions\":[" + mission("1", "EASY") + "," + mission("2", "EASY") + ","
                + mission("3", "MEDIUM") + "," + mission("4", "HARD") + "]}");
    }

    @Test void rejectsDuplicateTitles() {
        assertInvalid("{\"missions\":[" + mission("같은 제목", "EASY") + "," + mission("같은 제목", "HARD") + "]}");
    }

    private void assertInvalid(String json) {
        respond(json);
        assertThatThrownBy(() -> client.recommend(context()))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INVALID_GEMINI_MISSION_RECOMMENDATION_RESULT));
    }
    private void respond(String json) { try { when(gateway.generate(anyString(), any())).thenReturn(mapper.readTree(json)); }
        catch (Exception e) { throw new AssertionError(e); } }
    private String mission(String title, String difficulty) { return "{\"title\":\"" + title
            + "\",\"content\":\"내용\",\"difficulty\":\"" + difficulty + "\"}"; }
    private MissionRecommendationContext context() { return new MissionRecommendationContext(
            1L, 2L, "모임", "소개", GatheringCategory.PROJECT, List.of("개발"), List.of("Java")); }
}
