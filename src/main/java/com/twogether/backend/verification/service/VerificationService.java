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

    public VerificationService(
            VerificationRepository verificationRepository,
            GatheringRepository gatheringRepository,
            GatheringMemberRepository gatheringMemberRepository,
            UserRepository userRepository
    ) {
        this.verificationRepository = verificationRepository;
        this.gatheringRepository = gatheringRepository;
        this.gatheringMemberRepository = gatheringMemberRepository;
        this.userRepository = userRepository;
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
}
