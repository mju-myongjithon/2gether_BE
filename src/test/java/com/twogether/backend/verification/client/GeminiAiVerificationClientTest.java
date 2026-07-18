package com.twogether.backend.verification.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.verification.config.VerificationAiProperties;
import com.twogether.backend.verification.domain.AiStatus;
import com.twogether.backend.verification.dto.ai.AiVerificationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.ResourceAccessException;

import java.net.SocketTimeoutException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GeminiAiVerificationClientTest {
    @Test void parsesApprovedResponse() { assertStatus("APPROVED", "활동이 일치합니다.", AiStatus.APPROVED); }
    @Test void parsesRejectedResponse() { assertStatus("REJECTED", "활동 근거가 부족합니다.", AiStatus.REJECTED); }
    @Test void rejectsPending() { assertInvalid("{\"status\":\"PENDING\",\"reason\":\"대기\"}"); }
    @Test void rejectsBlankReason() { assertInvalid("{\"status\":\"APPROVED\",\"reason\":\"  \"}"); }
    @Test void rejectsLongReason() { assertInvalid("{\"status\":\"APPROVED\",\"reason\":\"" + "가".repeat(301) + "\"}"); }

    @Test
    void reportsMalformedJson() {
        Fixture fixture = fixture("not-json", false);
        assertError(() -> fixture.client.verify(request()), ErrorCode.GEMINI_VERIFICATION_RESPONSE_PARSE_FAILED);
    }

    @Test
    void reportsGeminiCallFailure() {
        Fixture fixture = fixture("", true);
        assertError(() -> fixture.client.verify(request()), ErrorCode.GEMINI_VERIFICATION_CALL_FAILED);
    }

    @Test
    void reportsGeminiTimeout() {
        RestClient restClient = mock(RestClient.class);
        RestClient.RequestBodyUriSpec spec = mock(RestClient.RequestBodyUriSpec.class, RETURNS_SELF);
        when(restClient.post()).thenReturn(spec);
        when(spec.retrieve()).thenThrow(new ResourceAccessException("timeout", new SocketTimeoutException()));
        VerificationImageDownloader downloader = ignored -> new DownloadedImage(new byte[]{1}, "image/jpeg");
        GeminiAiVerificationClient client = new GeminiAiVerificationClient(
                properties(), new ObjectMapper(), restClient, downloader);

        assertError(() -> client.verify(request()), ErrorCode.GEMINI_VERIFICATION_TIMEOUT);
    }

    @Test
    void propagatesImageDownloadFailure() {
        VerificationImageDownloader downloader = ignored -> {
            throw new BusinessException(ErrorCode.VERIFICATION_IMAGE_DOWNLOAD_FAILED);
        };
        GeminiAiVerificationClient client = new GeminiAiVerificationClient(
                properties(), new ObjectMapper(), mock(RestClient.class), downloader);

        assertError(() -> client.verify(request()), ErrorCode.VERIFICATION_IMAGE_DOWNLOAD_FAILED);
    }

    private void assertStatus(String status, String reason, AiStatus expected) {
        Fixture fixture = fixture("{\"status\":\"" + status + "\",\"reason\":\"" + reason + "\"}", false);
        var result = fixture.client.verify(request());
        assertThat(result.status()).isEqualTo(expected);
        assertThat(result.reason()).isEqualTo(reason);
        fixture.server.verify();
    }

    private void assertInvalid(String modelJson) {
        Fixture fixture = fixture(modelJson, false);
        assertError(() -> fixture.client.verify(request()), ErrorCode.INVALID_GEMINI_VERIFICATION_RESULT);
    }

    private Fixture fixture(String modelJson, boolean serverError) {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        var expectation = server.expect(once(), requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-test:generateContent"))
                .andExpect(method(HttpMethod.POST)).andExpect(header("x-goog-api-key", "test-key"));
        if (serverError) expectation.andRespond(withServerError());
        else expectation.andRespond(withSuccess(envelope(modelJson), MediaType.APPLICATION_JSON));
        VerificationImageDownloader downloader = ignored -> new DownloadedImage(new byte[]{1, 2, 3}, "image/jpeg");
        return new Fixture(new GeminiAiVerificationClient(properties(), new ObjectMapper(), builder.build(), downloader), server);
    }

    private String envelope(String modelJson) {
        try {
            return new ObjectMapper().writeValueAsString(Map.of("candidates", List.of(Map.of(
                    "content", Map.of("parts", List.of(Map.of("text", modelJson)))))));
        } catch (Exception exception) { throw new AssertionError(exception); }
    }

    private VerificationAiProperties properties() {
        VerificationAiProperties value = new VerificationAiProperties();
        value.setApiKey("test-key");
        value.setModel("gemini-test");
        value.setAllowedImageDomains(List.of("images.example.com"));
        return value;
    }

    private AiVerificationRequest request() {
        return new AiVerificationRequest(1L, 2L, "러닝", "함께 달리기", null,
                "운동장", null, "https://images.example.com/a.jpg", "함께 달렸습니다.");
    }

    private void assertError(org.assertj.core.api.ThrowableAssert.ThrowingCallable callable, ErrorCode code) {
        assertThatThrownBy(callable).isInstanceOfSatisfying(BusinessException.class,
                exception -> assertThat(exception.getErrorCode()).isEqualTo(code));
    }
    private record Fixture(GeminiAiVerificationClient client, MockRestServiceServer server) { }
}
