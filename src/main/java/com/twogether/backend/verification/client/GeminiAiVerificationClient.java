package com.twogether.backend.verification.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.global.ai.ExternalAiFailureType;
import com.twogether.backend.global.ai.ExternalAiUnavailableException;
import com.twogether.backend.verification.config.VerificationAiProperties;
import com.twogether.backend.verification.domain.AiStatus;
import com.twogether.backend.verification.dto.ai.AiVerificationRequest;
import com.twogether.backend.verification.dto.ai.AiVerificationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.SocketTimeoutException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnExpression("'${app.ai.verification.mode:mock}' == 'gemini' or '${app.ai.verification.mode:mock}' == 'demo'")
public class GeminiAiVerificationClient implements AiVerificationClient {
    private static final String API_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";

    private final VerificationAiProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final VerificationImageDownloader imageDownloader;

    @Autowired
    public GeminiAiVerificationClient(VerificationAiProperties properties, ObjectMapper objectMapper,
                                      VerificationImageDownloader imageDownloader) {
        this(properties, objectMapper, createRestClient(properties), imageDownloader);
    }

    GeminiAiVerificationClient(VerificationAiProperties properties, ObjectMapper objectMapper,
                               RestClient restClient, VerificationImageDownloader imageDownloader) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = restClient;
        this.imageDownloader = imageDownloader;
    }

    @Override
    public AiVerificationResult verify(AiVerificationRequest request) {
        validateConfiguration();
        DownloadedImage image = imageDownloader.download(request.photoUrl());
        JsonNode response;
        try {
            response = restClient.post()
                    .uri(API_BASE_URL + properties.getModel() + ":generateContent")
                    .header("x-goog-api-key", properties.getApiKey())
                    .body(createRequestBody(request, image))
                    .retrieve()
                    .body(JsonNode.class);
        } catch (ResourceAccessException exception) {
            if (hasCause(exception, SocketTimeoutException.class)) {
                throw unavailable(ErrorCode.GEMINI_VERIFICATION_TIMEOUT, ExternalAiFailureType.READ_TIMEOUT);
            }
            throw unavailable(ErrorCode.GEMINI_VERIFICATION_CALL_FAILED, ExternalAiFailureType.CONNECTION_FAILED);
        } catch (RestClientResponseException exception) {
            int status = exception.getStatusCode().value();
            if (status == 429) throw unavailable(ErrorCode.GEMINI_VERIFICATION_CALL_FAILED, ExternalAiFailureType.RATE_LIMITED);
            if (status == 500 || status == 502 || status == 503 || status == 504) {
                throw unavailable(ErrorCode.GEMINI_VERIFICATION_CALL_FAILED, ExternalAiFailureType.SERVER_ERROR);
            }
            throw new BusinessException(ErrorCode.GEMINI_VERIFICATION_CALL_FAILED);
        } catch (RestClientException exception) {
            throw new BusinessException(ErrorCode.GEMINI_VERIFICATION_CALL_FAILED);
        }
        return parseAndValidate(response);
    }

    private Map<String, Object> createRequestBody(AiVerificationRequest request, DownloadedImage image) {
        String prompt = """
                당신은 모임 활동 인증 심사자입니다. 제공된 사진을 실제로 살펴보고 모임 정보와 후기를 함께 비교하세요.
                APPROVED는 사진에서 실제 활동 정황이 확인되고, 사진과 후기가 대체로 일치하며, 모임 내용과 관련 있을 때만 선택하세요.
                모임과 무관한 사진, 광고, 캡처 화면, 빈 이미지, 사진과 후기의 명백한 불일치, 판단 근거 부족은 REJECTED입니다.
                확신이 부족하면 APPROVED하지 마세요. 판정 이유는 구체적인 한국어 300자 이하로 작성하세요.

                모임 제목: %s
                모임 설명: %s
                카테고리: %s
                장소: %s
                모임 일시: %s
                인증 후기: %s
                """.formatted(
                safe(request.gatheringTitle()), safe(request.gatheringContent()),
                request.gatheringCategory() == null ? "" : request.gatheringCategory().name(),
                safe(request.gatheringLocation()), request.meetAt() == null ? "" : request.meetAt(),
                safe(request.reviewText())
        );
        Map<String, Object> schema = Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "status", Map.of("type", "STRING", "enum", List.of("APPROVED", "REJECTED")),
                        "reason", Map.of("type", "STRING")
                ),
                "required", List.of("status", "reason")
        );
        Map<String, Object> inlineData = Map.of(
                "mimeType", image.mimeType(),
                "data", Base64.getEncoder().encodeToString(image.bytes())
        );
        return Map.of(
                "contents", List.of(Map.of("role", "user", "parts", List.of(
                        Map.of("text", prompt), Map.of("inlineData", inlineData)
                ))),
                "generationConfig", Map.of(
                        "temperature", 0.1,
                        "responseMimeType", "application/json",
                        "responseSchema", schema
                )
        );
    }

    private AiVerificationResult parseAndValidate(JsonNode response) {
        final JsonNode result;
        try {
            String text = response.path("candidates").path(0).path("content")
                    .path("parts").path(0).path("text").asText(null);
            if (text == null) throw new JsonProcessingException("Gemini response text is missing") { };
            result = objectMapper.readTree(text);
        } catch (JsonProcessingException | NullPointerException exception) {
            throw new BusinessException(ErrorCode.GEMINI_VERIFICATION_RESPONSE_PARSE_FAILED);
        }

        if (!result.hasNonNull("status") || !result.path("status").isTextual()
                || !result.hasNonNull("reason") || !result.path("reason").isTextual()) {
            throw invalidResult();
        }
        final AiStatus status;
        try {
            status = AiStatus.valueOf(result.path("status").textValue());
        } catch (IllegalArgumentException exception) {
            throw invalidResult();
        }
        String reason = result.path("reason").textValue();
        if (status == AiStatus.PENDING || reason.isBlank() || reason.length() > 300) {
            throw invalidResult();
        }
        return new AiVerificationResult(status, reason);
    }

    private void validateConfiguration() {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()
                || properties.getModel() == null || properties.getModel().isBlank()
                || !properties.getModel().matches("[A-Za-z0-9._-]+")
                || properties.getConnectTimeout() == null || properties.getConnectTimeout().isNegative()
                || properties.getReadTimeout() == null || properties.getReadTimeout().isNegative()
                || properties.getMaxImageSize() <= 0 || properties.getAllowedImageDomains() == null
                || properties.getAllowedImageDomains().isEmpty()) {
            throw new BusinessException(ErrorCode.GEMINI_VERIFICATION_NOT_CONFIGURED);
        }
    }

    private String safe(Object value) { return value == null ? "" : value.toString(); }
    private BusinessException invalidResult() {
        return new BusinessException(ErrorCode.INVALID_GEMINI_VERIFICATION_RESULT);
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
    private static RestClient createRestClient(VerificationAiProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getConnectTimeout());
        factory.setReadTimeout(properties.getReadTimeout());
        return RestClient.builder().requestFactory(factory).build();
    }
}
