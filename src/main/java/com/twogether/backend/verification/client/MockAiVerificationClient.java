package com.twogether.backend.verification.client;

import com.twogether.backend.verification.domain.AiStatus;
import com.twogether.backend.verification.dto.ai.AiVerificationRequest;
import com.twogether.backend.verification.dto.ai.AiVerificationResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

/** 개발 및 Swagger 테스트용 결정론적 Mock이며 실제 AI 판정이 아니다. */
@Component
@ConditionalOnExpression("'${app.ai.verification.mode:mock}' == 'mock' or '${app.ai.verification.mode:mock}' == 'demo'")
public class MockAiVerificationClient implements AiVerificationClient {
    private static final String APPROVED_REASON =
            "활동 사진과 후기 내용을 확인하여 교류 활동으로 승인하였습니다.";
    private static final String REJECTED_REASON =
            "활동 내용을 확인하기 어렵습니다. 사진과 구체적인 후기를 다시 확인해주세요.";

    @Override
    public AiVerificationResult verify(AiVerificationRequest request) {
        boolean approved = hasText(request.photoUrl())
                && hasText(request.reviewText())
                && request.reviewText().replaceAll("\\s", "").length() >= 10;
        return approved
                ? new AiVerificationResult(AiStatus.APPROVED, APPROVED_REASON)
                : new AiVerificationResult(AiStatus.REJECTED, REJECTED_REASON);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
