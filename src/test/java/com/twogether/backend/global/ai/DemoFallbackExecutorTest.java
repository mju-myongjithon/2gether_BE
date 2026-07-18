package com.twogether.backend.global.ai;

import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DemoFallbackExecutorTest {
    @Test
    void returnsGeminiResultWithoutCallingMockOnSuccess() {
        AtomicBoolean mockCalled = new AtomicBoolean();
        String result = DemoFallbackExecutor.execute("test", LoggerFactory.getLogger(getClass()),
                () -> "gemini", () -> { mockCalled.set(true); return "mock"; });
        assertThat(result).isEqualTo("gemini");
        assertThat(mockCalled).isFalse();
    }

    @Test
    void fallsBackForRateLimitTimeoutAndServerErrors() {
        for (ExternalAiFailureType type : new ExternalAiFailureType[] {
                ExternalAiFailureType.RATE_LIMITED, ExternalAiFailureType.CONNECT_TIMEOUT,
                ExternalAiFailureType.READ_TIMEOUT, ExternalAiFailureType.SERVER_ERROR,
                ExternalAiFailureType.CONNECTION_FAILED}) {
            String result = DemoFallbackExecutor.execute("test", LoggerFactory.getLogger(getClass()),
                    () -> { throw new ExternalAiUnavailableException(ErrorCode.GEMINI_RECOMMENDATION_CALL_FAILED, type); },
                    () -> "mock");
            assertThat(result).isEqualTo("mock");
        }
    }

    @Test
    void doesNotFallbackForParsingOrBusinessValidationErrors() {
        assertThatThrownBy(() -> DemoFallbackExecutor.execute("test", LoggerFactory.getLogger(getClass()),
                () -> { throw new BusinessException(ErrorCode.GEMINI_RECOMMENDATION_RESPONSE_PARSE_FAILED); },
                () -> "mock")).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> DemoFallbackExecutor.execute("test", LoggerFactory.getLogger(getClass()),
                () -> { throw new BusinessException(ErrorCode.INVALID_GEMINI_RECOMMENDATION_RESULT); },
                () -> "mock")).isInstanceOf(BusinessException.class);
    }
}
