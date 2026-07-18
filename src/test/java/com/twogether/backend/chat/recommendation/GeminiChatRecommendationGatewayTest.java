package com.twogether.backend.chat.recommendation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GeminiChatRecommendationGatewayTest {
    @Test void extractsStructuredJson() {
        Fixture fixture = fixture("{\"topics\":[]}", false);
        assertThat(fixture.gateway.generate("prompt", Map.of("type", "OBJECT")).path("topics").isArray()).isTrue();
        fixture.server.verify();
    }

    @Test void reportsMalformedJson() {
        assertError(() -> fixture("not-json", false).gateway.generate("prompt", Map.of()),
                ErrorCode.GEMINI_CHAT_RECOMMENDATION_RESPONSE_PARSE_FAILED);
    }

    @Test void reportsCallFailure() {
        assertError(() -> fixture("", true).gateway.generate("prompt", Map.of()),
                ErrorCode.GEMINI_CHAT_RECOMMENDATION_CALL_FAILED);
    }

    @Test void reportsTimeout() {
        RestClient restClient = mock(RestClient.class);
        RestClient.RequestBodyUriSpec spec = mock(RestClient.RequestBodyUriSpec.class, RETURNS_SELF);
        when(restClient.post()).thenReturn(spec);
        when(spec.retrieve()).thenThrow(new ResourceAccessException("timeout", new SocketTimeoutException()));
        GeminiChatRecommendationGateway gateway = new GeminiChatRecommendationGateway(properties(), new ObjectMapper(), restClient);
        assertError(() -> gateway.generate("prompt", Map.of()), ErrorCode.GEMINI_CHAT_RECOMMENDATION_TIMEOUT);
    }

    @Test void rejectsMissingConfiguration() {
        ChatRecommendationAiProperties properties = properties();
        properties.setApiKey(" ");
        GeminiChatRecommendationGateway gateway = new GeminiChatRecommendationGateway(
                properties, new ObjectMapper(), mock(RestClient.class));
        assertError(() -> gateway.generate("prompt", Map.of()), ErrorCode.GEMINI_CHAT_RECOMMENDATION_NOT_CONFIGURED);
    }

    private Fixture fixture(String modelJson, boolean serverError) {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        var expectation = server.expect(once(), requestTo(
                        "https://generativelanguage.googleapis.com/v1beta/models/gemini-test:generateContent"))
                .andExpect(method(HttpMethod.POST)).andExpect(header("x-goog-api-key", "test-key"));
        if (serverError) expectation.andRespond(withServerError());
        else expectation.andRespond(withSuccess(envelope(modelJson), MediaType.APPLICATION_JSON));
        return new Fixture(new GeminiChatRecommendationGateway(properties(), new ObjectMapper(), builder.build()), server);
    }

    private String envelope(String modelJson) {
        try {
            return new ObjectMapper().writeValueAsString(Map.of("candidates", List.of(Map.of(
                    "content", Map.of("parts", List.of(Map.of("text", modelJson)))))));
        } catch (Exception exception) { throw new AssertionError(exception); }
    }

    private ChatRecommendationAiProperties properties() {
        ChatRecommendationAiProperties properties = new ChatRecommendationAiProperties();
        properties.setApiKey("test-key");
        properties.setModel("gemini-test");
        return properties;
    }

    private void assertError(org.assertj.core.api.ThrowableAssert.ThrowingCallable callable, ErrorCode code) {
        assertThatThrownBy(callable).isInstanceOfSatisfying(BusinessException.class,
                exception -> assertThat(exception.getErrorCode()).isEqualTo(code));
    }
    private record Fixture(GeminiChatRecommendationGateway gateway, MockRestServiceServer server) { }
}
