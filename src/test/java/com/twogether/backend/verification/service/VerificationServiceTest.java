package com.twogether.backend.verification.service;

import com.twogether.backend.gathering.domain.Gathering;
import com.twogether.backend.gathering.domain.GatheringStatus;
import com.twogether.backend.gathering.repository.GatheringMemberRepository;
import com.twogether.backend.gathering.repository.GatheringRepository;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.user.domain.User;
import com.twogether.backend.user.repository.UserRepository;
import com.twogether.backend.verification.domain.AiStatus;
import com.twogether.backend.verification.domain.Verification;
import com.twogether.backend.verification.dto.request.VerificationCreateRequest;
import com.twogether.backend.verification.repository.VerificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VerificationServiceTest {

    private VerificationRepository verificationRepository;
    private GatheringRepository gatheringRepository;
    private GatheringMemberRepository gatheringMemberRepository;
    private UserRepository userRepository;
    private VerificationService verificationService;
    private Gathering gathering;
    private User uploader;

    @BeforeEach
    void setUp() {
        verificationRepository = mock(VerificationRepository.class);
        gatheringRepository = mock(GatheringRepository.class);
        gatheringMemberRepository = mock(GatheringMemberRepository.class);
        userRepository = mock(UserRepository.class);
        verificationService = new VerificationService(
                verificationRepository, gatheringRepository,
                gatheringMemberRepository, userRepository
        );
        gathering = mock(Gathering.class);
        uploader = mock(User.class);
        when(gathering.getId()).thenReturn(10L);
        when(uploader.getId()).thenReturn(20L);
        when(userRepository.findByAuthUserId("auth-id")).thenReturn(Optional.of(uploader));
        when(gatheringRepository.findById(10L)).thenReturn(Optional.of(gathering));
    }

    @Test
    void memberSubmitsVerificationForCompletedGatheringAsPending() {
        allowSubmission();
        when(verificationRepository.save(any(Verification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = verificationService.create(
                "auth-id", 10L,
                new VerificationCreateRequest("https://storage.example.com/activity.jpg", "활동 후기")
        );

        ArgumentCaptor<Verification> captor = ArgumentCaptor.forClass(Verification.class);
        verify(verificationRepository).save(captor.capture());
        Verification saved = captor.getValue();
        assertThat(response.aiStatus()).isEqualTo(AiStatus.PENDING);
        assertThat(saved.getAiReason()).isNull();
        assertThat(saved.getRejectionReason()).isNull();
        assertThat(saved.getCanvasPixelX()).isNull();
        assertThat(saved.getCanvasPixelY()).isNull();
        assertThat(saved.getVerifiedAt()).isNull();
    }

    @Test
    void nonMemberCannotSubmitVerification() {
        when(gathering.getStatus()).thenReturn(GatheringStatus.COMPLETED);
        when(gatheringMemberRepository.existsByGatheringIdAndUserId(10L, 20L)).thenReturn(false);

        assertError(ErrorCode.VERIFICATION_SUBMIT_FORBIDDEN);
    }

    @Test
    void verificationCannotBeSubmittedBeforeGatheringCompletion() {
        when(gathering.getStatus()).thenReturn(GatheringStatus.CONFIRMED);

        assertError(ErrorCode.VERIFICATION_NOT_COMPLETED_GATHERING);
    }

    @Test
    void duplicateVerificationCannotBeSubmittedForSameGathering() {
        when(gathering.getStatus()).thenReturn(GatheringStatus.COMPLETED);
        when(gatheringMemberRepository.existsByGatheringIdAndUserId(10L, 20L)).thenReturn(true);
        when(verificationRepository.existsByGatheringId(10L)).thenReturn(true);

        assertError(ErrorCode.VERIFICATION_ALREADY_EXISTS);
    }

    private void allowSubmission() {
        when(gathering.getStatus()).thenReturn(GatheringStatus.COMPLETED);
        when(gatheringMemberRepository.existsByGatheringIdAndUserId(10L, 20L)).thenReturn(true);
        when(verificationRepository.existsByGatheringId(10L)).thenReturn(false);
    }

    private void assertError(ErrorCode errorCode) {
        assertThatThrownBy(() -> verificationService.create(
                "auth-id", 10L,
                new VerificationCreateRequest("https://storage.example.com/activity.jpg", null)
        ))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(errorCode);
        verify(verificationRepository, never()).save(any());
    }
}
