package com.twogether.backend.contentrecommendation.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twogether.backend.contentrecommendation.config.ContentRecommendationAiProperties;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadGateway;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(OutputCaptureExtension.class)
class WebSearchAiContentRecommendationClientTest {
    @Test void parsesGroundedSearchResponseAndSendsGoogleSearchTool() {
        Fixture fixture = fixture();
        fixture.server.expect(once(), method(HttpMethod.POST))
                .andExpect(header("x-goog-api-key", "test-key"))
                .andExpect(jsonPath("$.tools[0].google_search").exists())
                .andExpect(jsonPath("$.generationConfig").doesNotExist())
                .andExpect(jsonPath("$.contents[0].parts[0].text").value(org.hamcrest.Matchers.containsString("JSON 객체만 반환")))
                .andRespond(withSuccess(response("https://spring.io/guides", "https://spring.io/guides"), MediaType.APPLICATION_JSON));

        var result = fixture.client.recommend(context());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).matchedTags()).containsExactly("Spring");
        fixture.server.verify();
    }

    @Test void rejectsUrlMissingFromGroundingEvidence() {
        Fixture fixture = fixture();
        fixture.server.expect(once(), method(HttpMethod.POST)).andRespond(withSuccess(
                response("https://invented.example/page", "https://spring.io/guides"), MediaType.APPLICATION_JSON));
        assertError(() -> fixture.client.recommend(context()), ErrorCode.INVALID_GEMINI_CONTENT_RECOMMENDATION_RESULT);
    }

    @Test void rejectsMalformedJson() {
        Fixture fixture = fixture();
        fixture.server.expect(once(), method(HttpMethod.POST)).andRespond(withSuccess(
                "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"not-json\"}]}}]}", MediaType.APPLICATION_JSON));
        assertError(() -> fixture.client.recommend(context()), ErrorCode.GEMINI_CONTENT_RECOMMENDATION_RESPONSE_PARSE_FAILED);
    }

    @Test void parsesJsonInsideMarkdownCodeFence() {
        Fixture fixture = fixture();
        fixture.server.expect(once(), method(HttpMethod.POST)).andRespond(withSuccess(
                responseWithText("```json\n{\"recommendations\":[{\"title\":\"Spring Guide\","
                        + "\"description\":\"Official guide\",\"url\":\"https://spring.io/guides\","
                        + "\"contentType\":\"BLOG\",\"matchedTags\":[\"Spring\"]}]}\n```", "https://spring.io/guides"),
                MediaType.APPLICATION_JSON));

        assertThat(fixture.client.recommend(context())).hasSize(1);
    }

    @Test void doesNotLogExternalErrorBodyOrApiKey(CapturedOutput output) {
        Fixture fixture = fixture();
        fixture.server.expect(once(), method(HttpMethod.POST)).andRespond(withStatus(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"error\":{\"status\":\"INVALID_ARGUMENT\",\"message\":\"bad test-key\"}}"));

        assertError(() -> fixture.client.recommend(context()), ErrorCode.GEMINI_CONTENT_RECOMMENDATION_CALL_FAILED);

        assertThat(output).doesNotContain("INVALID_ARGUMENT", "bad test-key", "test-key");
    }

    @Test void reportsCallFailure() {
        Fixture fixture = fixture();
        fixture.server.expect(once(), method(HttpMethod.POST)).andRespond(withBadGateway());
        assertError(() -> fixture.client.recommend(context()), ErrorCode.GEMINI_CONTENT_RECOMMENDATION_CALL_FAILED);
    }

    private Fixture fixture() {
        ContentRecommendationAiProperties properties = new ContentRecommendationAiProperties();
        properties.setApiKey("test-key");
        properties.setModel("gemini-test");
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        return new Fixture(new WebSearchAiContentRecommendationClient(properties, new ObjectMapper(), builder.build()), server);
    }

    private ContentRecommendationContext context() {
        return new ContentRecommendationContext(1L, List.of("Spring"), null, 2);
    }

    private String response(String recommendationUrl, String groundingUrl) {
        String text = "{\\\"recommendations\\\":[{\\\"title\\\":\\\"Spring Guide\\\",\\\"description\\\":\\\"Official guide\\\","
                + "\\\"url\\\":\\\"" + recommendationUrl + "\\\",\\\"contentType\\\":\\\"BLOG\\\",\\\"matchedTags\\\":[\\\"Spring\\\"]}]}";
        return "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"" + text
                + "\"}]},\"groundingMetadata\":{\"groundingChunks\":[{\"web\":{\"uri\":\""
                + groundingUrl + "\",\"title\":\"Guide\"}}]}}]}";
    }

    private String responseWithText(String text, String groundingUrl) {
        try {
            String escapedText = new ObjectMapper().writeValueAsString(text);
            return "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":" + escapedText
                    + "}]},\"groundingMetadata\":{\"groundingChunks\":[{\"web\":{\"uri\":\""
                    + groundingUrl + "\"}}]}}]}";
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }

    private void assertError(Runnable action, ErrorCode expected) {
        assertThatThrownBy(action::run).isInstanceOfSatisfying(BusinessException.class,
                error -> assertThat(error.getErrorCode()).isEqualTo(expected));
    }

    private record Fixture(WebSearchAiContentRecommendationClient client, MockRestServiceServer server) { }
}
