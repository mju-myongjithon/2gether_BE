package com.twogether.backend.global.ai;

import org.slf4j.Logger;

import java.util.function.Supplier;

public final class DemoFallbackExecutor {
    private DemoFallbackExecutor() {
    }

    public static <T> T execute(String feature, Logger log, Supplier<T> gemini, Supplier<T> mock) {
        try {
            return gemini.get();
        } catch (ExternalAiUnavailableException exception) {
            log.warn("AI demo fallback: feature={}, externalError={}", feature, exception.getFailureType());
            return mock.get();
        }
    }
}
