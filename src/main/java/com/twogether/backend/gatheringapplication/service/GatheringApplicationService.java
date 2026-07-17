package com.twogether.backend.gatheringapplication.service;

import com.twogether.backend.department.domain.Department;
import com.twogether.backend.department.repository.DepartmentRepository;
import com.twogether.backend.gathering.domain.Gathering;
import com.twogether.backend.gathering.domain.GatheringMember;
import com.twogether.backend.gathering.repository.GatheringMemberRepository;
import com.twogether.backend.gathering.repository.GatheringRepository;
import com.twogether.backend.gatheringapplication.domain.ApplicationStatus;
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
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.global.response.PageResponse;
import com.twogether.backend.notification.domain.NotificationType;
import com.twogether.backend.notification.service.NotificationDispatchService;
import com.twogether.backend.user.domain.User;
import com.twogether.backend.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class GatheringApplicationService {

    private static final List<ApplicationStatus> ACTIVE_APPLICATION_STATUSES =
            List.of(ApplicationStatus.PENDING, ApplicationStatus.ACCEPTED);

    private final GatheringApplicationRepository gatheringApplicationRepository;
    private final GatheringRepository gatheringRepository;
    private final GatheringMemberRepository gatheringMemberRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
        private final NotificationDispatchService notificationDispatchService;

    public GatheringApplicationService(
            GatheringApplicationRepository gatheringApplicationRepository,
            GatheringRepository gatheringRepository,
            GatheringMemberRepository gatheringMemberRepository,
            UserRepository userRepository,
                        DepartmentRepository departmentRepository,
                        NotificationDispatchService notificationDispatchService
    ) {
        this.gatheringApplicationRepository = gatheringApplicationRepository;
        this.gatheringRepository = gatheringRepository;
        this.gatheringMemberRepository = gatheringMemberRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
                this.notificationDispatchService = notificationDispatchService;
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

        notificationDispatchService.notifyEvent(
                gathering.getHost().getId(),
                NotificationType.GATHERING_APPLICATION,
                "새 모임 신청",
                me.getNickname() + "님이 '" + gathering.getTitle() + "' 모임에 신청했습니다.",
                Map.of(
                        "gatheringId", gathering.getId(),
                        "applicationId", application.getId()
                )
        );

        return new GatheringApplicationCreateResponse(
                application.getId(),
                gathering.getId(),
                application.getStatus(),
                application.getAppliedAt()
        );
    }

    /**
     * 방장의 신청함 조회(선택적 status 필터, 페이징).
     * 신청자(user)를 함께 로딩하고 학과명은 배치로 매핑한다. 정렬은 신청 최신순.
     *
     * 신청자의 취미/기술 태그는 user_tag(사용자-태그 연결) 도메인 미구현으로
     * 현재 빈 목록으로 반환한다(해당 도메인 구축 후 채움). campus 는 기능 제외로 null.
     */
    public PageResponse<GatheringApplicationResponse> getApplications(
            String authUserId,
            Long gatheringId,
            String status,
            int page,
            int size
    ) {
        User me = userRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GATHERING_NOT_FOUND));

        if (!gathering.isHost(me.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        ApplicationStatus statusFilter = parseStatus(status);
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "appliedAt")
        );

        Page<GatheringApplication> applicationPage = (statusFilter == null)
                ? gatheringApplicationRepository.findByGatheringId(gatheringId, pageable)
                : gatheringApplicationRepository.findByGatheringIdAndStatus(gatheringId, statusFilter, pageable);

        List<GatheringApplication> applications = applicationPage.getContent();
        Map<Long, String> departmentNameById = resolveDepartmentNames(applications);

        List<GatheringApplicationResponse> content = applications.stream()
                .map(application -> toApplicationResponse(application, departmentNameById))
                .toList();

        return PageResponse.of(
                content,
                page,
                size,
                applicationPage.getTotalElements()
        );
    }

    /**
     * 로그인한 사용자의 신청 목록을 최신순으로 조회한다.
     */
    public PageResponse<MyApplicationResponse> getMyApplications(
            String authUserId,
            int page,
            int size
    ) {
        User me = userRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "appliedAt")
        );

        Page<GatheringApplication> applicationPage = gatheringApplicationRepository.findByUserId(me.getId(), pageable);

        List<MyApplicationResponse> content = applicationPage.getContent().stream()
                .map(application -> new MyApplicationResponse(
                        application.getId(),
                        application.getStatus(),
                        application.getAppliedAt(),
                        new GatheringBriefResponse(
                                application.getGathering().getId(),
                                application.getGathering().getTitle(),
                                application.getGathering().getCategory().name(),
                                application.getGathering().getStatus()
                        )
                ))
                .toList();

        return PageResponse.of(
                content,
                page,
                size,
                applicationPage.getTotalElements()
        );
    }

        /**
         * 로그인한 사용자의 본인 신청을 취소한다.
         *
         * PENDING 신청만 취소 가능하며, 취소 시 신청 레코드를 삭제한다.
         */
        @Transactional
        public void cancelMyApplication(
                        String authUserId,
                        Long applicationId
        ) {
                User me = userRepository.findByAuthUserId(authUserId)
                                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

                GatheringApplication application = gatheringApplicationRepository.findDetailById(applicationId)
                                .orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND));

                if (!application.getUser().getId().equals(me.getId())) {
                        throw new BusinessException(ErrorCode.FORBIDDEN);
                }
                if (!application.isPending()) {
                        throw new BusinessException(ErrorCode.APPLICATION_ALREADY_PROCESSED);
                }

                gatheringApplicationRepository.deleteByGatheringIdAndUserIdAndStatus(
                                application.getGathering().getId(),
                                me.getId(),
                                ApplicationStatus.PENDING
                );
        }

    /**
     * 신청 수락(원자적).
     *
     * 정원 동시성 제어를 위해 모임 행에 비관적 락을 건 뒤,
     * status = ACCEPTED + gathering_member 생성 + current_members 증가를 한 트랜잭션으로 처리한다.
     */
    @Transactional
    public GatheringApplicationAcceptResponse accept(
            String authUserId,
            Long applicationId
    ) {
        User me = userRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        GatheringApplication application = gatheringApplicationRepository.findDetailById(applicationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND));

        // 정원 경쟁 방지: 모임 행을 잠근 상태로 검증·증가
        Gathering gathering = gatheringRepository.findByIdForUpdate(application.getGathering().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.GATHERING_NOT_FOUND));

        if (!gathering.isHost(me.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (!application.isPending()) {
            throw new BusinessException(ErrorCode.APPLICATION_ALREADY_PROCESSED);
        }
        if (!gathering.isRecruiting()) {
            throw new BusinessException(ErrorCode.GATHERING_NOT_RECRUITING);
        }

        User applicant = application.getUser();
        if (gatheringMemberRepository.existsByGatheringIdAndUserId(gathering.getId(), applicant.getId())) {
            throw new BusinessException(ErrorCode.ALREADY_MEMBER);
        }
        if (gathering.isFull()) {
            throw new BusinessException(ErrorCode.CAPACITY_EXCEEDED);
        }

        GatheringMember member = gatheringMemberRepository.save(
                GatheringMember.member(gathering, applicant)
        );
        gathering.increaseMember();
        application.accept();

        notificationDispatchService.notifyEvent(
                applicant.getId(),
                NotificationType.APPLICATION_ACCEPTED,
                "모임 신청이 수락되었습니다",
                "'" + gathering.getTitle() + "' 모임 신청이 수락되었습니다.",
                Map.of(
                        "gatheringId", gathering.getId(),
                        "applicationId", application.getId()
                )
        );

        return new GatheringApplicationAcceptResponse(
                application.getId(),
                application.getStatus(),
                member.getId(),
                application.getReviewedAt()
        );
    }

    /**
     * 신청 거절(사유 저장). 신청 기록은 남는다.
     */
    @Transactional
    public GatheringApplicationRejectResponse reject(
            String authUserId,
            Long applicationId,
            GatheringApplicationRejectRequest request
    ) {
        User me = userRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        GatheringApplication application = gatheringApplicationRepository.findDetailById(applicationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND));

        if (!application.getGathering().isHost(me.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (!application.isPending()) {
            throw new BusinessException(ErrorCode.APPLICATION_ALREADY_PROCESSED);
        }

        application.reject(request.rejectReason());

        return new GatheringApplicationRejectResponse(
                application.getId(),
                application.getStatus(),
                application.getRejectReason(),
                application.getReviewedAt()
        );
    }

    private ApplicationStatus parseStatus(
            String status
    ) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return ApplicationStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }

    private Map<Long, String> resolveDepartmentNames(
            List<GatheringApplication> applications
    ) {
        List<Long> departmentIds = applications.stream()
                .map(application -> application.getUser().getDepartmentId())
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (departmentIds.isEmpty()) {
            return Map.of();
        }

        return departmentRepository.findAllById(departmentIds).stream()
                .collect(Collectors.toMap(Department::getId, Department::getName));
    }

    private GatheringApplicationResponse toApplicationResponse(
            GatheringApplication application,
            Map<Long, String> departmentNameById
    ) {
        User applicant = application.getUser();

        ApplicantResponse applicantResponse = new ApplicantResponse(
                applicant.getId(),
                applicant.getNickname(),
                departmentNameById.get(applicant.getDepartmentId()),
                // 캠퍼스 비율 기능 제외 → campus null
                null,
                // user_tag 도메인 미구현 → 취미/기술 태그는 빈 목록
                List.of(),
                List.of()
        );

        return new GatheringApplicationResponse(
                application.getId(),
                application.getStatus(),
                application.getMessage(),
                application.getAppliedAt(),
                applicantResponse
        );
    }
}
