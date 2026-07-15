package com.twogether.backend.gatheringapplication.service;

import com.twogether.backend.gathering.domain.Gathering;
import com.twogether.backend.gathering.repository.GatheringRepository;
import com.twogether.backend.gatheringapplication.domain.GatheringApplication;
import com.twogether.backend.gatheringapplication.dto.request.GatheringApplicationCreateRequest;
import com.twogether.backend.gatheringapplication.dto.request.GatheringApplicationRejectRequest;
import com.twogether.backend.gatheringapplication.dto.response.ApplicantResponse;
import com.twogether.backend.gatheringapplication.dto.response.GatheringApplicationAcceptResponse;
import com.twogether.backend.gatheringapplication.dto.response.GatheringApplicationCreateResponse;
import com.twogether.backend.gatheringapplication.dto.response.GatheringApplicationRejectResponse;
import com.twogether.backend.gatheringapplication.dto.response.GatheringApplicationResponse;
import com.twogether.backend.gatheringapplication.dto.response.GatheringBriefResponse;
import com.twogether.backend.gatheringapplication.dto.response.MyApplicationResponse;
import com.twogether.backend.gatheringapplication.repository.GatheringApplicationRepository;
import com.twogether.backend.gatheringmember.domain.GatheringMember;
import com.twogether.backend.gatheringmember.domain.GatheringMemberRole;
import com.twogether.backend.gatheringmember.repository.GatheringMemberRepository;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.global.response.PageResponse;
import com.twogether.backend.user.domain.User;
import com.twogether.backend.user.repository.UserRepository;
import com.twogether.backend.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class GatheringApplicationService {

    private static final ZoneOffset KST = ZoneOffset.of("+09:00");

    private final GatheringApplicationRepository gatheringApplicationRepository;
    private final GatheringRepository gatheringRepository;
    private final GatheringMemberRepository gatheringMemberRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public GatheringApplicationService(
            GatheringApplicationRepository gatheringApplicationRepository,
            GatheringRepository gatheringRepository,
            GatheringMemberRepository gatheringMemberRepository,
            UserRepository userRepository,
            UserService userService
    ) {
        this.gatheringApplicationRepository = gatheringApplicationRepository;
        this.gatheringRepository = gatheringRepository;
        this.gatheringMemberRepository = gatheringMemberRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    /**
     * 모집 중인 모임에 참여를 신청합니다. 같은 모임에는 한 번만 신청할 수 있습니다.
     */
    @Transactional
    public GatheringApplicationCreateResponse apply(
            String authUserId,
            Long gatheringId,
            GatheringApplicationCreateRequest request
    ) {
        User user = userService.findOrCreateUser(authUserId);
        Gathering gathering = findGathering(gatheringId);

        if (!gathering.isRecruiting()) {
            throw new BusinessException(ErrorCode.GATHERING_NOT_RECRUITING);
        }

        if (gatheringApplicationRepository.existsByGatheringIdAndUserId(gatheringId, user.getId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_APPLICATION);
        }

        GatheringApplication application = gatheringApplicationRepository.save(
                new GatheringApplication(gatheringId, user.getId(), request.message())
        );

        return new GatheringApplicationCreateResponse(
                application.getId(),
                gatheringId,
                application.getStatus(),
                toOffsetDateTime(application.getAppliedAt())
        );
    }

    /**
     * 방장이 특정 모임의 신청자 목록을 조회합니다.
     */
    public PageResponse<GatheringApplicationResponse> getApplications(
            String authUserId,
            Long gatheringId,
            int page,
            int size
    ) {
        User user = userService.findOrCreateUser(authUserId);
        Gathering gathering = findGathering(gatheringId);

        if (!gathering.isHost(user.getId())) {
            throw new BusinessException(ErrorCode.GATHERING_NOT_HOST);
        }

        Page<GatheringApplication> applications = gatheringApplicationRepository
                .findByGatheringIdOrderByAppliedAtDesc(gatheringId, PageRequest.of(page, size));

        List<GatheringApplicationResponse> content = applications.getContent()
                .stream()
                .map(this::toApplicationResponse)
                .toList();

        return PageResponse.of(content, page, size, applications.getTotalElements());
    }

    /**
     * 현재 로그인한 사용자가 신청한 모임 목록을 조회합니다.
     */
    public PageResponse<MyApplicationResponse> getMyApplications(
            String authUserId,
            int page,
            int size
    ) {
        User user = userService.findOrCreateUser(authUserId);

        Page<GatheringApplication> applications = gatheringApplicationRepository
                .findByUserIdOrderByAppliedAtDesc(user.getId(), PageRequest.of(page, size));

        List<MyApplicationResponse> content = applications.getContent()
                .stream()
                .map(this::toMyApplicationResponse)
                .toList();

        return PageResponse.of(content, page, size, applications.getTotalElements());
    }

    /**
     * 방장이 신청을 수락합니다. 수락 시 gathering_member가 생성됩니다.
     */
    @Transactional
    public GatheringApplicationAcceptResponse accept(
            String authUserId,
            Long applicationId
    ) {
        User user = userService.findOrCreateUser(authUserId);
        GatheringApplication application = findApplication(applicationId);
        Gathering gathering = findGathering(application.getGatheringId());

        if (!gathering.isHost(user.getId())) {
            throw new BusinessException(ErrorCode.GATHERING_NOT_HOST);
        }

        if (!application.isPending()) {
            throw new BusinessException(ErrorCode.APPLICATION_ALREADY_REVIEWED);
        }

        if (!gathering.isRecruiting()) {
            throw new BusinessException(ErrorCode.GATHERING_NOT_RECRUITING);
        }

        long currentMemberCount = gatheringMemberRepository
                .countByGatheringIdAndLeftAtIsNull(gathering.getId());

        if (currentMemberCount >= gathering.getMaxMembers()) {
            throw new BusinessException(ErrorCode.GATHERING_FULL);
        }

        application.accept();

        GatheringMember member = gatheringMemberRepository.save(
                new GatheringMember(
                        gathering.getId(),
                        application.getUserId(),
                        GatheringMemberRole.MEMBER
                )
        );

        return new GatheringApplicationAcceptResponse(
                application.getId(),
                application.getStatus(),
                member.getId(),
                toOffsetDateTime(application.getReviewedAt())
        );
    }

    /**
     * 방장이 신청을 거절합니다. 거절되어도 신청 기록은 남습니다.
     */
    @Transactional
    public GatheringApplicationRejectResponse reject(
            String authUserId,
            Long applicationId,
            GatheringApplicationRejectRequest request
    ) {
        User user = userService.findOrCreateUser(authUserId);
        GatheringApplication application = findApplication(applicationId);
        Gathering gathering = findGathering(application.getGatheringId());

        if (!gathering.isHost(user.getId())) {
            throw new BusinessException(ErrorCode.GATHERING_NOT_HOST);
        }

        if (!application.isPending()) {
            throw new BusinessException(ErrorCode.APPLICATION_ALREADY_REVIEWED);
        }

        application.reject(request.rejectReason());

        return new GatheringApplicationRejectResponse(
                application.getId(),
                application.getStatus(),
                application.getRejectReason(),
                toOffsetDateTime(application.getReviewedAt())
        );
    }

    private Gathering findGathering(Long gatheringId) {
        return gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GATHERING_NOT_FOUND));
    }

    private GatheringApplication findApplication(Long applicationId) {
        return gatheringApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND));
    }

    private GatheringApplicationResponse toApplicationResponse(GatheringApplication application) {
        ApplicantResponse applicant = userRepository.findById(application.getUserId())
                .map(user -> new ApplicantResponse(
                        user.getId(),
                        user.getNickname(),

                        // TODO: Department/Tag 엔티티가 아직 없어 학과명/캠퍼스/태그는 임시로 비워둠
                        null,
                        null,
                        List.of(),
                        List.of()
                ))
                .orElse(new ApplicantResponse(
                        application.getUserId(), null, null, null, List.of(), List.of()
                ));

        return new GatheringApplicationResponse(
                application.getId(),
                application.getStatus(),
                application.getMessage(),
                toOffsetDateTime(application.getAppliedAt()),
                applicant
        );
    }

    private MyApplicationResponse toMyApplicationResponse(GatheringApplication application) {
        GatheringBriefResponse gathering = gatheringRepository.findById(application.getGatheringId())
                .map(g -> new GatheringBriefResponse(g.getId(), g.getTitle(), g.getCategory(), g.getStatus()))
                .orElse(new GatheringBriefResponse(application.getGatheringId(), null, null, null));

        return new MyApplicationResponse(
                application.getId(),
                application.getStatus(),
                toOffsetDateTime(application.getAppliedAt()),
                gathering
        );
    }

    private static OffsetDateTime toOffsetDateTime(LocalDateTime localDateTime) {
        return localDateTime == null
                ? null
                : localDateTime.atOffset(KST);
    }
}
