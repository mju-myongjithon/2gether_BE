package com.twogether.backend.gathering.service;

import com.twogether.backend.gathering.domain.Gathering;
import com.twogether.backend.gathering.domain.GatheringImage;
import com.twogether.backend.gathering.domain.GatheringStatus;
import com.twogether.backend.gathering.domain.RecruitPhase;
import com.twogether.backend.gathering.dto.request.GatheringCreateRequest;
import com.twogether.backend.gathering.dto.request.GatheringUpdateRequest;
import com.twogether.backend.gathering.dto.response.CampusRatioResponse;
import com.twogether.backend.gathering.dto.response.GatheringCancelResponse;
import com.twogether.backend.gathering.dto.response.GatheringConfirmResponse;
import com.twogether.backend.gathering.dto.response.GatheringCreateResponse;
import com.twogether.backend.gathering.dto.response.GatheringDetailResponse;
import com.twogether.backend.gathering.dto.response.GatheringImageResponse;
import com.twogether.backend.gathering.dto.response.GatheringSummaryResponse;
import com.twogether.backend.gathering.dto.response.GatheringUpdateResponse;
import com.twogether.backend.gathering.dto.response.HostSummaryResponse;
import com.twogether.backend.gathering.repository.GatheringImageRepository;
import com.twogether.backend.gathering.repository.GatheringRepository;
import com.twogether.backend.gatheringapplication.domain.ApplicationStatus;
import com.twogether.backend.gatheringapplication.domain.GatheringApplication;
import com.twogether.backend.gatheringapplication.repository.GatheringApplicationRepository;
import com.twogether.backend.gatheringmember.domain.GatheringMember;
import com.twogether.backend.gatheringmember.domain.GatheringMemberRole;
import com.twogether.backend.gatheringmember.dto.response.GatheringMemberResponse;
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

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class GatheringService {

    private static final ZoneOffset KST = ZoneOffset.of("+09:00");

    private final GatheringRepository gatheringRepository;
    private final GatheringImageRepository gatheringImageRepository;
    private final GatheringMemberRepository gatheringMemberRepository;
    private final GatheringApplicationRepository gatheringApplicationRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public GatheringService(
            GatheringRepository gatheringRepository,
            GatheringImageRepository gatheringImageRepository,
            GatheringMemberRepository gatheringMemberRepository,
            GatheringApplicationRepository gatheringApplicationRepository,
            UserRepository userRepository,
            UserService userService
    ) {
        this.gatheringRepository = gatheringRepository;
        this.gatheringImageRepository = gatheringImageRepository;
        this.gatheringMemberRepository = gatheringMemberRepository;
        this.gatheringApplicationRepository = gatheringApplicationRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    /**
     * 모임을 생성합니다. 생성자는 자동으로 gathering_member에 HOST로 등록됩니다.
     */
    @Transactional
    public GatheringCreateResponse create(
            String authUserId,
            GatheringCreateRequest request
    ) {
        User host = userService.findOrCreateUser(authUserId);

        Gathering gathering = new Gathering(
                host.getId(),
                request.title(),
                request.content(),
                request.category(),
                request.location(),
                request.maxMembers(),
                request.fusionEnabled(),
                toLocalDateTime(request.recruitStartAt()),
                toLocalDateTime(request.recruitEndAt()),
                toLocalDateTime(request.meetAt())
        );

        gathering = gatheringRepository.save(gathering);

        saveImages(gathering.getId(), request.imageUrls());

        gatheringMemberRepository.save(
                new GatheringMember(
                        gathering.getId(),
                        host.getId(),
                        GatheringMemberRole.HOST
                )
        );

        return new GatheringCreateResponse(
                gathering.getId(),
                host.getId(),
                gathering.getTitle(),
                gathering.getStatus(),
                toOffsetDateTime(gathering.getCreatedAt())
        );
    }

    /**
     * 카테고리, 상태, 검색어 기준으로 모임 목록을 조회합니다. 로그인 없이 조회할 수 있습니다.
     */
    public PageResponse<GatheringSummaryResponse> list(
            String category,
            GatheringStatus status,
            String keyword,
            int page,
            int size
    ) {
        Page<Gathering> gatherings = gatheringRepository.search(
                category,
                status,
                keyword,
                PageRequest.of(page, size)
        );

        List<GatheringSummaryResponse> content = gatherings.getContent()
                .stream()
                .map(this::toSummaryResponse)
                .toList();

        return PageResponse.of(
                content,
                page,
                size,
                gatherings.getTotalElements()
        );
    }

    /**
     * 모임 상세 정보를 조회합니다. 로그인 없이도 조회할 수 있으며,
     * 로그인한 경우에만 myApplicationStatus/isHost/isMember를 계산합니다.
     */
    public GatheringDetailResponse getDetail(
            Long gatheringId,
            String authUserIdOrNull
    ) {
        Gathering gathering = findGathering(gatheringId);

        List<GatheringMember> activeMembers =
                gatheringMemberRepository
                        .findByGatheringIdAndLeftAtIsNullOrderByJoinedAtAsc(gatheringId);

        List<GatheringMemberResponse> members = activeMembers.stream()
                .map(this::toMemberResponse)
                .toList();

        List<GatheringImageResponse> images =
                gatheringImageRepository
                        .findByGatheringIdOrderBySortOrderAsc(gatheringId)
                        .stream()
                        .map(this::toImageResponse)
                        .toList();

        HostSummaryResponse host = toHostSummary(gathering.getHostId());

        // TODO: User의 campus 분류 로직이 아직 없어 캠퍼스 비율은 임시로 0/0을 반환합니다.
        CampusRatioResponse campusRatio = new CampusRatioResponse(0, 0);

        ApplicationStatus myApplicationStatus = null;
        boolean isHost = false;
        boolean isMember = false;

        if (authUserIdOrNull != null) {
            User currentUser = userService.findOrCreateUser(authUserIdOrNull);

            isHost = gathering.isHost(currentUser.getId());
            isMember = gatheringMemberRepository
                    .existsByGatheringIdAndUserIdAndLeftAtIsNull(gatheringId, currentUser.getId());
            myApplicationStatus = gatheringApplicationRepository
                    .findByGatheringIdAndUserId(gatheringId, currentUser.getId())
                    .map(GatheringApplication::getStatus)
                    .orElse(null);
        }

        return new GatheringDetailResponse(
                gathering.getId(),
                gathering.getTitle(),
                gathering.getContent(),
                gathering.getCategory(),
                gathering.getLocation(),
                gathering.getMaxMembers(),
                activeMembers.size(),
                gathering.isFusionEnabled(),
                gathering.getStatus(),
                calculateRecruitPhase(gathering),
                toOffsetDateTime(gathering.getRecruitStartAt()),
                toOffsetDateTime(gathering.getRecruitEndAt()),
                toOffsetDateTime(gathering.getMeetAt()),
                toOffsetDateTime(gathering.getCreatedAt()),
                images,
                host,
                members,
                campusRatio,
                myApplicationStatus,
                isHost,
                isMember
        );
    }

    /**
     * 모집 중인 모임을 방장이 수정합니다. 이미지 목록은 전달된 값으로 전체 교체됩니다.
     */
    @Transactional
    public GatheringUpdateResponse update(
            String authUserId,
            Long gatheringId,
            GatheringUpdateRequest request
    ) {
        User user = userService.findOrCreateUser(authUserId);
        Gathering gathering = findGathering(gatheringId);

        validateHost(gathering, user.getId());
        validateRecruiting(gathering);

        gathering.update(
                request.title(),
                request.content(),
                request.category(),
                request.location(),
                request.maxMembers(),
                request.fusionEnabled(),
                toLocalDateTime(request.recruitStartAt()),
                toLocalDateTime(request.recruitEndAt()),
                toLocalDateTime(request.meetAt())
        );

        gatheringImageRepository.deleteByGatheringId(gatheringId);
        saveImages(gatheringId, request.imageUrls());

        return new GatheringUpdateResponse(
                gathering.getId(),
                toOffsetDateTime(gathering.getUpdatedAt())
        );
    }

    /**
     * 모집 중인 모임을 방장이 취소합니다. 물리 삭제가 아니라 status = CANCELED로 변경합니다.
     */
    @Transactional
    public GatheringCancelResponse cancel(
            String authUserId,
            Long gatheringId
    ) {
        User user = userService.findOrCreateUser(authUserId);
        Gathering gathering = findGathering(gatheringId);

        validateHost(gathering, user.getId());
        validateRecruiting(gathering);

        gathering.cancel();

        return new GatheringCancelResponse(
                gathering.getId(),
                gathering.getStatus(),
                toOffsetDateTime(gathering.getCanceledAt())
        );
    }

    /**
     * 방장이 모집을 마감합니다. status = CONFIRMED로 변경합니다.
     *
     * 채팅 도메인이 아직 실제 엔티티로 구현되지 않아, 그룹 채팅방 자동 생성은
     * 후속 작업으로 남겨두고 chatRoomId는 임시로 null을 반환합니다.
     */
    @Transactional
    public GatheringConfirmResponse confirm(
            String authUserId,
            Long gatheringId
    ) {
        User user = userService.findOrCreateUser(authUserId);
        Gathering gathering = findGathering(gatheringId);

        validateHost(gathering, user.getId());
        validateRecruiting(gathering);

        gathering.confirm();

        return new GatheringConfirmResponse(
                gathering.getId(),
                gathering.getStatus(),
                toOffsetDateTime(gathering.getConfirmedAt()),
                null
        );
    }

    Gathering findGathering(Long gatheringId) {
        return gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GATHERING_NOT_FOUND));
    }

    void validateRecruiting(Gathering gathering) {
        if (!gathering.isRecruiting()) {
            throw new BusinessException(ErrorCode.GATHERING_NOT_RECRUITING);
        }
    }

    private void validateHost(Gathering gathering, Long userId) {
        if (!gathering.isHost(userId)) {
            throw new BusinessException(ErrorCode.GATHERING_NOT_HOST);
        }
    }

    private void saveImages(Long gatheringId, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }

        if (imageUrls.size() > GatheringImage.MAX_IMAGE_COUNT) {
            throw new BusinessException(ErrorCode.INVALID_GATHERING_IMAGE_COUNT);
        }

        for (int i = 0; i < imageUrls.size(); i++) {
            gatheringImageRepository.save(
                    new GatheringImage(gatheringId, imageUrls.get(i), i)
            );
        }
    }

    RecruitPhase calculateRecruitPhase(Gathering gathering) {
        if (gathering.getStatus() != GatheringStatus.RECRUITING) {
            return RecruitPhase.CLOSED;
        }

        LocalDateTime now = LocalDateTime.now();

        if (gathering.getRecruitEndAt() == null) {
            return RecruitPhase.ALWAYS_OPEN;
        }

        if (gathering.getRecruitStartAt() != null
                && now.isBefore(gathering.getRecruitStartAt())) {
            return RecruitPhase.UPCOMING;
        }

        if (!now.isBefore(gathering.getRecruitEndAt())) {
            return RecruitPhase.CLOSED;
        }

        return RecruitPhase.OPEN;
    }

    private GatheringSummaryResponse toSummaryResponse(Gathering gathering) {
        int currentMemberCount = (int) gatheringMemberRepository
                .countByGatheringIdAndLeftAtIsNull(gathering.getId());

        List<GatheringImage> images = gatheringImageRepository
                .findByGatheringIdOrderBySortOrderAsc(gathering.getId());

        String thumbnailImageUrl = images.isEmpty()
                ? null
                : images.get(0).getImageUrl();

        return new GatheringSummaryResponse(
                gathering.getId(),
                gathering.getTitle(),
                gathering.getCategory(),
                gathering.getLocation(),
                gathering.getMaxMembers(),
                currentMemberCount,
                gathering.isFusionEnabled(),
                gathering.getStatus(),
                calculateRecruitPhase(gathering),
                toOffsetDateTime(gathering.getRecruitStartAt()),
                toOffsetDateTime(gathering.getRecruitEndAt()),
                toOffsetDateTime(gathering.getMeetAt()),
                thumbnailImageUrl,
                toHostSummary(gathering.getHostId())
        );
    }

    private HostSummaryResponse toHostSummary(Long hostId) {
        return userRepository.findById(hostId)
                .map(user -> new HostSummaryResponse(
                        user.getId(),
                        user.getNickname(),

                        // TODO: Department 엔티티가 아직 없어 학과명/캠퍼스는 임시로 null
                        null,
                        null
                ))
                .orElse(new HostSummaryResponse(hostId, null, null, null));
    }

    private GatheringMemberResponse toMemberResponse(GatheringMember member) {
        return userRepository.findById(member.getUserId())
                .map(user -> new GatheringMemberResponse(
                        user.getId(),
                        user.getNickname(),
                        member.getRole(),

                        // TODO: Department 엔티티가 아직 없어 학과명/캠퍼스는 임시로 null
                        null,
                        null
                ))
                .orElse(new GatheringMemberResponse(
                        member.getUserId(), null, member.getRole(), null, null
                ));
    }

    private GatheringImageResponse toImageResponse(GatheringImage image) {
        return new GatheringImageResponse(
                image.getId(),
                image.getImageUrl(),
                image.getSortOrder()
        );
    }

    private static LocalDateTime toLocalDateTime(OffsetDateTime offsetDateTime) {
        return offsetDateTime == null
                ? null
                : offsetDateTime.atZoneSameInstant(KST).toLocalDateTime();
    }

    private static OffsetDateTime toOffsetDateTime(LocalDateTime localDateTime) {
        return localDateTime == null
                ? null
                : localDateTime.atOffset(KST);
    }
}
