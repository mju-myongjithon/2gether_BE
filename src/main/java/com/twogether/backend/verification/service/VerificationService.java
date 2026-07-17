package com.twogether.backend.verification.service;

import com.twogether.backend.gathering.domain.Gathering;
import com.twogether.backend.gathering.domain.GatheringStatus;
import com.twogether.backend.gathering.repository.GatheringMemberRepository;
import com.twogether.backend.gathering.repository.GatheringRepository;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.user.domain.User;
import com.twogether.backend.user.repository.UserRepository;
import com.twogether.backend.verification.domain.Verification;
import com.twogether.backend.verification.client.AiVerificationClient;
import com.twogether.backend.verification.dto.ai.AiVerificationRequest;
import com.twogether.backend.verification.dto.ai.AiVerificationResult;
import com.twogether.backend.verification.dto.request.VerificationCreateRequest;
import com.twogether.backend.verification.dto.response.VerificationResponse;
import com.twogether.backend.verification.repository.VerificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class VerificationService {

    private final VerificationRepository verificationRepository;
    private final GatheringRepository gatheringRepository;
    private final GatheringMemberRepository gatheringMemberRepository;
    private final UserRepository userRepository;
    private final AiVerificationClient aiVerificationClient;

    public VerificationService(
            VerificationRepository verificationRepository,
            GatheringRepository gatheringRepository,
            GatheringMemberRepository gatheringMemberRepository,
            UserRepository userRepository,
            AiVerificationClient aiVerificationClient
    ) {
        this.verificationRepository = verificationRepository;
        this.gatheringRepository = gatheringRepository;
        this.gatheringMemberRepository = gatheringMemberRepository;
        this.userRepository = userRepository;
        this.aiVerificationClient = aiVerificationClient;
    }

    @Transactional
    public VerificationResponse create(
            String authUserId,
            Long gatheringId,
            VerificationCreateRequest request
    ) {
        User uploader = userRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GATHERING_NOT_FOUND));

        if (gathering.getStatus() != GatheringStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.VERIFICATION_NOT_COMPLETED_GATHERING);
        }
        if (!gatheringMemberRepository.existsByGatheringIdAndUserId(gatheringId, uploader.getId())) {
            throw new BusinessException(ErrorCode.VERIFICATION_SUBMIT_FORBIDDEN);
        }
        if (verificationRepository.existsByGatheringId(gatheringId)) {
            throw new BusinessException(ErrorCode.VERIFICATION_ALREADY_EXISTS);
        }

        Verification verification = verificationRepository.save(
                new Verification(gathering, uploader, request.photoUrl(), request.reviewText())
        );

        return VerificationResponse.from(verification);
    }

    @Transactional
    public VerificationResponse evaluate(String authUserId, Long verificationId) {
        User user = userRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Verification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VERIFICATION_NOT_FOUND));
        Gathering gathering = verification.getGathering();

        if (!gatheringMemberRepository.existsByGatheringIdAndUserId(gathering.getId(), user.getId())) {
            throw new BusinessException(ErrorCode.VERIFICATION_EVALUATE_FORBIDDEN);
        }
        if (verification.getAiStatus() != com.twogether.backend.verification.domain.AiStatus.PENDING) {
            throw new BusinessException(ErrorCode.VERIFICATION_ALREADY_EVALUATED);
        }

        AiVerificationResult result = aiVerificationClient.verify(new AiVerificationRequest(
                verification.getId(), gathering.getId(), gathering.getTitle(), gathering.getContent(),
                gathering.getCategory(), gathering.getLocation(), gathering.getMeetAt(),
                verification.getPhotoUrl(), verification.getReviewText()
        ));
        validateAiResult(result);
        if (result.status() == com.twogether.backend.verification.domain.AiStatus.APPROVED) {
            verification.approve(result.reason());
        } else {
            verification.reject(result.reason());
        }
        return VerificationResponse.from(verification);
    }

    private void validateAiResult(AiVerificationResult result) {
        if (result == null || result.status() == null
                || result.status() == com.twogether.backend.verification.domain.AiStatus.PENDING
                || result.reason() == null || result.reason().isBlank()
                || result.reason().length() > 300) {
            throw new BusinessException(ErrorCode.INVALID_AI_VERIFICATION_RESULT);
        }
    }
}
