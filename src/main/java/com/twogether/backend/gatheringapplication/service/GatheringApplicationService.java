package com.twogether.backend.gatheringapplication.service;

import com.twogether.backend.gathering.domain.Gathering;
import com.twogether.backend.gathering.repository.GatheringMemberRepository;
import com.twogether.backend.gathering.repository.GatheringRepository;
import com.twogether.backend.gatheringapplication.domain.ApplicationStatus;
import com.twogether.backend.gatheringapplication.domain.GatheringApplication;
import com.twogether.backend.gatheringapplication.dto.request.GatheringApplicationCreateRequest;
import com.twogether.backend.gatheringapplication.dto.response.GatheringApplicationCreateResponse;
import com.twogether.backend.gatheringapplication.repository.GatheringApplicationRepository;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.user.domain.User;
import com.twogether.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GatheringApplicationService {

    private static final List<ApplicationStatus> ACTIVE_APPLICATION_STATUSES =
            List.of(ApplicationStatus.PENDING, ApplicationStatus.ACCEPTED);

    private final GatheringApplicationRepository gatheringApplicationRepository;
    private final GatheringRepository gatheringRepository;
    private final GatheringMemberRepository gatheringMemberRepository;
    private final UserRepository userRepository;

    public GatheringApplicationService(
            GatheringApplicationRepository gatheringApplicationRepository,
            GatheringRepository gatheringRepository,
            GatheringMemberRepository gatheringMemberRepository,
            UserRepository userRepository
    ) {
        this.gatheringApplicationRepository = gatheringApplicationRepository;
        this.gatheringRepository = gatheringRepository;
        this.gatheringMemberRepository = gatheringMemberRepository;
        this.userRepository = userRepository;
    }

    /**
     * 모임 참가 신청.
     *
     * 모집중 검증 → 이미 멤버(방장 포함) 차단 → 활성 신청 중복 차단 → PENDING 신청 저장.
     * REJECTED 이력이 있어도 활성 신청이 없으면 재신청이 가능하다.
     */
    @Transactional
    public GatheringApplicationCreateResponse apply(
            String authUserId,
            Long gatheringId,
            GatheringApplicationCreateRequest request
    ) {
        User me = userRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GATHERING_NOT_FOUND));

        if (!gathering.isRecruiting()) {
            throw new BusinessException(ErrorCode.GATHERING_NOT_RECRUITING);
        }

        // 방장은 HOST 멤버로 등록돼 있으므로 멤버 검사로 함께 차단된다.
        if (gatheringMemberRepository.existsByGatheringIdAndUserId(gatheringId, me.getId())) {
            throw new BusinessException(ErrorCode.ALREADY_MEMBER);
        }

        if (gatheringApplicationRepository.existsByGatheringIdAndUserIdAndStatusIn(
                gatheringId, me.getId(), ACTIVE_APPLICATION_STATUSES)) {
            throw new BusinessException(ErrorCode.DUPLICATE_APPLICATION);
        }

        GatheringApplication application = gatheringApplicationRepository.save(
                GatheringApplication.create(gathering, me, request.message())
        );

        return new GatheringApplicationCreateResponse(
                application.getId(),
                gathering.getId(),
                application.getStatus(),
                application.getAppliedAt()
        );
    }
}
