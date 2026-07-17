package com.twogether.backend.verification.service;

import com.twogether.backend.gathering.domain.Gathering;
import com.twogether.backend.gathering.repository.GatheringMemberRepository;
import com.twogether.backend.gathering.repository.GatheringRepository;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.user.domain.User;
import com.twogether.backend.user.repository.UserRepository;
import com.twogether.backend.verification.client.AiVerificationClient;
import com.twogether.backend.verification.client.MockAiVerificationClient;
import com.twogether.backend.verification.domain.AiStatus;
import com.twogether.backend.verification.domain.Verification;
import com.twogether.backend.verification.dto.ai.AiVerificationRequest;
import com.twogether.backend.verification.dto.ai.AiVerificationResult;
import com.twogether.backend.verification.repository.VerificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VerificationEvaluationTest {
    private VerificationRepository verificationRepository;
    private GatheringMemberRepository memberRepository;
    private UserRepository userRepository;
    private AiVerificationClient client;
    private VerificationService service;
    private Gathering gathering;
    private User user;
    private Verification verification;

    @BeforeEach
    void setUp() {
        verificationRepository = mock(VerificationRepository.class);
        memberRepository = mock(GatheringMemberRepository.class);
        userRepository = mock(UserRepository.class);
        client = mock(AiVerificationClient.class);
        service = new VerificationService(verificationRepository, mock(GatheringRepository.class),
                memberRepository, userRepository, client);
        gathering = mock(Gathering.class);
        user = mock(User.class);
        when(gathering.getId()).thenReturn(10L);
        when(user.getId()).thenReturn(20L);
        verification = new Verification(gathering, user, "https://example.com/photo.jpg",
                "모임원들과 함께 즐거운 교류 활동을 진행했습니다.");
        when(userRepository.findByAuthUserId("auth-id")).thenReturn(Optional.of(user));
        when(verificationRepository.findById(1L)).thenReturn(Optional.of(verification));
        when(memberRepository.existsByGatheringIdAndUserId(10L, 20L)).thenReturn(true);
    }

    @Test
    void approvedResultIsApplied() {
        when(client.verify(any())).thenReturn(new AiVerificationResult(AiStatus.APPROVED, "승인 사유"));

        var response = service.evaluate("auth-id", 1L);

        assertThat(response.aiStatus()).isEqualTo(AiStatus.APPROVED);
        assertThat(response.aiReason()).isEqualTo("승인 사유");
        assertThat(response.verifiedAt()).isNotNull();
        assertUnassignedFieldsRemainNull();
    }

    @Test
    void rejectedResultIsApplied() {
        when(client.verify(any())).thenReturn(new AiVerificationResult(AiStatus.REJECTED, "반려 사유"));

        var response = service.evaluate("auth-id", 1L);

        assertThat(response.aiStatus()).isEqualTo(AiStatus.REJECTED);
        assertThat(response.aiReason()).isEqualTo("반려 사유");
        assertThat(response.verifiedAt()).isNotNull();
        assertUnassignedFieldsRemainNull();
    }

    @Test
    void nonMemberCannotEvaluate() {
        when(memberRepository.existsByGatheringIdAndUserId(10L, 20L)).thenReturn(false);
        assertError(ErrorCode.VERIFICATION_EVALUATE_FORBIDDEN);
        verify(client, never()).verify(any());
    }

    @Test
    void missingVerificationCannotBeEvaluated() {
        when(verificationRepository.findById(1L)).thenReturn(Optional.empty());
        assertError(ErrorCode.VERIFICATION_NOT_FOUND);
    }

    @Test
    void approvedAndRejectedVerificationCannotBeEvaluatedAgain() {
        verification.approve("최초 승인");
        assertError(ErrorCode.VERIFICATION_ALREADY_EVALUATED);

        verification = new Verification(gathering, user, "photo", "review");
        verification.reject("최초 반려");
        when(verificationRepository.findById(1L)).thenReturn(Optional.of(verification));
        assertError(ErrorCode.VERIFICATION_ALREADY_EVALUATED);
    }

    @Test
    void mockClientReturnsSameDeterministicResults() {
        MockAiVerificationClient mockClient = new MockAiVerificationClient();
        AiVerificationRequest request = new AiVerificationRequest(1L, 10L, "제목", "내용",
                null, "장소", null, "photo", "공백 제외 열 글자 이상인 구체적 후기");
        assertThat(mockClient.verify(request)).isEqualTo(mockClient.verify(request));

        AiVerificationRequest rejected = new AiVerificationRequest(1L, 10L, "제목", "내용",
                null, "장소", null, "photo", "짧음");
        assertThat(mockClient.verify(rejected).status()).isEqualTo(AiStatus.REJECTED);
    }

    private void assertUnassignedFieldsRemainNull() {
        assertThat(verification.getRejectionReason()).isNull();
        assertThat(verification.getCanvasPixelX()).isNull();
        assertThat(verification.getCanvasPixelY()).isNull();
    }

    private void assertError(ErrorCode expected) {
        assertThatThrownBy(() -> service.evaluate("auth-id", 1L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(expected);
    }
}
