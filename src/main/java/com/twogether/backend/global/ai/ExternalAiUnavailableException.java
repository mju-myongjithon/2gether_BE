package com.twogether.backend.global.ai;

import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;

public class ExternalAiUnavailableException extends BusinessException {
    private final ExternalAiFailureType failureType;

    public ExternalAiUnavailableException(ErrorCode errorCode, ExternalAiFailureType failureType) {
        super(errorCode);
        this.failureType = failureType;
    }

    public ExternalAiFailureType getFailureType() {
        return failureType;
    }
}
