package com.twogether.backend.recommendation.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.recommendation.config.MemberRecommendationAiProperties;
import com.twogether.backend.recommendation.dto.request.AiCandidateInfo;
import com.twogether.backend.recommendation.dto.request.AiGatheringInfo;
import com.twogether.backend.recommendation.dto.request.AiGatheringRecommendationRequest;
import com.twogether.backend.recommendation.dto.response.AiRecommendationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GeminiAiRecommendationClientTest {

    @Test
    void parsesNormalResponse() {
        Fixture fixture = fixture("{\"recommendations\":[{\"userId\":14,\"score\":90,\"reason\":\"태그가 잘 맞습니다.\"}]}");

        AiRecommendationResponse response = fixture.client.recommendMembers(request());

        assertThat(response.recommendations()).hasSize(1);
        assertThat(response.recommendations().get(0).userId()).isEqualTo(14L);
        fixture.server.verify();
    }

    @Test
    void rejectsUserOutsideCandidatePool() {
        assertInvalid("{\"recommendations\":[{\"userId\":99,\"score\":90,\"reason\":\"적합합니다.\"}]}");
    }

    @Test
    void rejectsDuplicateUser() {
        assertInvalid("{\"recommendations\":[{\"userId\":14,\"score\":90,\"reason\":\"적합합니다.\"},{\"userId\":14,\"score\":80,\"reason\":\"역시 적합합니다.\"}]}");
    }

    @Test
    void rejectsInvalidScore() {
        assertInvalid("{\"recommendations\":[{\"userId\":14,\"score\":101,\"reason\":\"적합합니다.\"}]}");
    }

    @Test
    void rejectsBlankReason() {
        assertInvalid("{\"recommendations\":[{\"userId\":14,\"score\":90,\"reason\":\"  \"}]}");
    }

    @Test
    void reportsResponseParseFailure() {
        Fixture fixture = fixture("not-json");

        assertThatThrownBy(() -> fixture.client.recommendMembers(request()))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.GEMINI_RECOMMENDATION_RESPONSE_PARSE_FAILED));
    }

    private void assertInvalid(String modelJson) {
        Fixture fixture = fixture(modelJson);
        assertThatThrownBy(() -> fixture.client.recommendMembers(request()))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_GEMINI_RECOMMENDATION_RESULT));
    }

    private Fixture fixture(String modelJson) {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(once(), requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-test:generateContent"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("x-goog-api-key", "test-key"))
                .andRespond(withSuccess(geminiEnvelope(modelJson), MediaType.APPLICATION_JSON));
        MemberRecommendationAiProperties properties = properties();
        return new Fixture(new GeminiAiRecommendationClient(properties, new ObjectMapper(), builder.build()), server);
    }

    private String geminiEnvelope(String modelJson) {
        try {
            return new ObjectMapper().writeValueAsString(java.util.Map.of(
                    "candidates", List.of(java.util.Map.of("content", java.util.Map.of(
                            "parts", List.of(java.util.Map.of("text", modelJson)))))));
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }

    private MemberRecommendationAiProperties properties() {
        MemberRecommendationAiProperties properties = new MemberRecommendationAiProperties();
        properties.setApiKey("test-key");
        properties.setModel("gemini-test");
        return properties;
    }

    private AiGatheringRecommendationRequest request() {
        AiGatheringInfo gathering = new AiGatheringInfo(1L, "해커톤", "서비스 개발", "HACKATHON", 6, 2, true, List.of());
        return new AiGatheringRecommendationRequest(gathering, List.of(
                new AiCandidateInfo(14L, "후보1", 1L, "자연캠", "백엔드", 2, List.of()),
                new AiCandidateInfo(15L, "후보2", 2L, "인문캠", "기획", 1, List.of())
        ), 2);
    }

    private record Fixture(GeminiAiRecommendationClient client, MockRestServiceServer server) { }
}
