package com.twogether.backend.gathering.service;

import com.twogether.backend.department.domain.Department;
import com.twogether.backend.department.repository.DepartmentRepository;
import com.twogether.backend.gathering.domain.Gathering;
import com.twogether.backend.gathering.domain.GatheringCategory;
import com.twogether.backend.gathering.domain.GatheringImage;
import com.twogether.backend.gathering.domain.GatheringMember;
import com.twogether.backend.gathering.domain.GatheringStatus;
import com.twogether.backend.gathering.domain.GatheringTag;
import com.twogether.backend.gathering.dto.request.GatheringCreateRequest;
import com.twogether.backend.gathering.dto.request.GatheringUpdateRequest;
import com.twogether.backend.gathering.dto.response.GatheringCancelResponse;
import com.twogether.backend.gathering.dto.response.GatheringCreateResponse;
import com.twogether.backend.gathering.dto.response.GatheringDetailResponse;
import com.twogether.backend.gathering.dto.response.GatheringSummaryResponse;
import com.twogether.backend.gathering.dto.response.GatheringUpdateResponse;
import com.twogether.backend.gatheringapplication.domain.ApplicationStatus;
import com.twogether.backend.gatheringmember.dto.response.GatheringMemberResponse;
import com.twogether.backend.gathering.repository.GatheringImageRepository;
import com.twogether.backend.gathering.repository.GatheringMemberRepository;
import com.twogether.backend.gathering.repository.GatheringRepository;
import com.twogether.backend.gathering.repository.GatheringSpecification;
import com.twogether.backend.gathering.repository.GatheringTagName;
import com.twogether.backend.gathering.repository.GatheringTagRepository;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.global.response.PageResponse;
import com.twogether.backend.tag.repository.TagRepository;
import com.twogether.backend.user.domain.User;
import com.twogether.backend.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class GatheringService {

    private final GatheringRepository gatheringRepository;
    private final GatheringMemberRepository gatheringMemberRepository;
    private final GatheringTagRepository gatheringTagRepository;
    private final GatheringImageRepository gatheringImageRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    public GatheringService(
            GatheringRepository gatheringRepository,
            GatheringMemberRepository gatheringMemberRepository,
            GatheringTagRepository gatheringTagRepository,
            GatheringImageRepository gatheringImageRepository,
            TagRepository tagRepository,
            UserRepository userRepository,
            DepartmentRepository departmentRepository
    ) {
        this.gatheringRepository = gatheringRepository;
        this.gatheringMemberRepository = gatheringMemberRepository;
        this.gatheringTagRepository = gatheringTagRepository;
        this.gatheringImageRepository = gatheringImageRepository;
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
    }

    /**
     * 모임 목록 조회(필터·검색·페이징).
     *
     * category/status/keyword 는 동적 조건(Specification)으로 조합하고,
     * host 는 EntityGraph 로 함께 로딩한다. 태그명과 방장 학과명은
     * 각각 한 번의 배치 IN 쿼리로 조회해 N+1 을 방지한다.
     * 정렬은 created_at DESC 로 고정한다.
     */
    public PageResponse<GatheringSummaryResponse> getGatherings(
            String category,
            String status,
            String keyword,
            int page,
            int size
    ) {
        GatheringCategory categoryFilter = parseCategory(category);
        GatheringStatus statusFilter = parseStatus(status);

        Specification<Gathering> spec = Specification.allOf(
                GatheringSpecification.categoryEquals(categoryFilter),
                GatheringSpecification.statusEquals(statusFilter),
                GatheringSpecification.keywordContains(keyword)
        );

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Gathering> gatheringPage = gatheringRepository.findAll(spec, pageable);
        List<Gathering> gatherings = gatheringPage.getContent();

        Map<Long, List<String>> tagsByGathering = loadTagsByGathering(gatherings);
        Map<Long, String> departmentNameById = loadHostDepartmentNames(gatherings);

        OffsetDateTime now = OffsetDateTime.now();
        List<GatheringSummaryResponse> content = gatherings.stream()
                .map(gathering -> GatheringSummaryResponse.of(
                        gathering,
                        tagsByGathering.getOrDefault(gathering.getId(), List.of()),
                        departmentNameById.get(gathering.getHost().getDepartmentId()),
                        now
                ))
                .toList();

        return PageResponse.of(
                content,
                page,
                size,
                gatheringPage.getTotalElements()
        );
    }

    private GatheringCategory parseCategory(
            String category
    ) {
        if (category == null || category.isBlank()) {
            return null;
        }
        try {
            return GatheringCategory.valueOf(category.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }

    private GatheringStatus parseStatus(
            String status
    ) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return GatheringStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }

    private Map<Long, List<String>> loadTagsByGathering(
            List<Gathering> gatherings
    ) {
        if (gatherings.isEmpty()) {
            return Map.of();
        }

        List<Long> gatheringIds = gatherings.stream()
                .map(Gathering::getId)
                .toList();

        return gatheringTagRepository.findTagNamesByGatheringIds(gatheringIds).stream()
                .collect(Collectors.groupingBy(
                        GatheringTagName::getGatheringId,
                        Collectors.mapping(GatheringTagName::getTagName, Collectors.toList())
                ));
    }

    private Map<Long, String> loadHostDepartmentNames(
            List<Gathering> gatherings
    ) {
        List<Long> departmentIds = gatherings.stream()
                .map(gathering -> gathering.getHost().getDepartmentId())
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (departmentIds.isEmpty()) {
            return Map.of();
        }

        return departmentRepository.findAllById(departmentIds).stream()
                .collect(Collectors.toMap(Department::getId, Department::getName));
    }

    /**
     * 모임 상세 조회.
     *
     * host(fetch join)·멤버(user fetch join)·태그·이미지를 조립한다.
     * authUserId 가 null(비로그인)이거나 매칭 사용자가 없으면
     * isHost/isMember 는 false, myApplicationStatus 는 null 이다.
     *
     * myApplicationStatus 는 신청(application) 도메인 구현(이슈8) 전까지 항상 null 이며,
     * 해당 이슈에서 실제 조회로 대체한다.
     */
    public GatheringDetailResponse getGatheringDetail(
            Long gatheringId,
            String authUserId
    ) {
        Gathering gathering = gatheringRepository.findDetailById(gatheringId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GATHERING_NOT_FOUND));

        List<GatheringMember> members =
                gatheringMemberRepository.findByGatheringIdWithUser(gatheringId);

        Map<Long, String> departmentNameById = resolveDepartmentNames(gathering, members);

        List<GatheringMemberResponse> memberResponses = members.stream()
                .map(member -> new GatheringMemberResponse(
                        member.getUser().getId(),
                        member.getUser().getNickname(),
                        member.getRole(),
                        departmentNameById.get(member.getUser().getDepartmentId()),
                        // 캠퍼스 비율 기능 제외 → campus 는 null 처리(설계 확정)
                        null
                ))
                .toList();

        List<String> tags = gatheringTagRepository
                .findTagNamesByGatheringIds(List.of(gatheringId)).stream()
                .map(GatheringTagName::getTagName)
                .toList();

        List<String> images = gatheringImageRepository
                .findByGatheringIdOrderBySortOrderAsc(gatheringId).stream()
                .map(GatheringImage::getImageUrl)
                .toList();

        boolean isHost = false;
        boolean isMember = false;
        if (authUserId != null) {
            Long myUserId = userRepository.findByAuthUserId(authUserId)
                    .map(User::getId)
                    .orElse(null);
            if (myUserId != null) {
                isHost = gathering.isHost(myUserId);
                isMember = gatheringMemberRepository
                        .existsByGatheringIdAndUserId(gatheringId, myUserId);
            }
        }

        // 신청(application) 도메인 미구현 → 이슈8에서 실제 조회로 대체
        ApplicationStatus myApplicationStatus = null;

        return GatheringDetailResponse.of(
                gathering,
                departmentNameById.get(gathering.getHost().getDepartmentId()),
                memberResponses,
                tags,
                images,
                myApplicationStatus,
                isHost,
                isMember,
                OffsetDateTime.now()
        );
    }

    private Map<Long, String> resolveDepartmentNames(
            Gathering gathering,
            List<GatheringMember> members
    ) {
        List<Long> departmentIds = Stream.concat(
                        Stream.of(gathering.getHost()),
                        members.stream().map(GatheringMember::getUser)
                )
                .map(User::getDepartmentId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (departmentIds.isEmpty()) {
            return Map.of();
        }

        return departmentRepository.findAllById(departmentIds).stream()
                .collect(Collectors.toMap(Department::getId, Department::getName));
    }

    /**
     * 모임 부분 수정.
     *
     * 방장·모집중(RECRUITING) 조건을 검증한 뒤, 요청에 담긴 필드만 반영한다.
     * null 필드는 기존 값을 유지하고, tagIds/imageUrls 는 목록이 오면 통째로 교체한다
     * (빈 목록이면 전체 삭제, null이면 유지).
     */
    @Transactional
    public GatheringUpdateResponse update(
            String authUserId,
            Long gatheringId,
            GatheringUpdateRequest request
    ) {
        User me = userRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GATHERING_NOT_FOUND));

        if (!gathering.isHost(me.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (!gathering.isRecruiting()) {
            throw new BusinessException(ErrorCode.GATHERING_NOT_MODIFIABLE);
        }

        // null = 기존 값 유지
        String title = request.title() != null ? request.title() : gathering.getTitle();
        String content = request.content() != null ? request.content() : gathering.getContent();
        String location = request.location() != null ? request.location() : gathering.getLocation();
        boolean fusionEnabled = request.fusionEnabled() != null
                ? request.fusionEnabled()
                : gathering.isFusionEnabled();
        OffsetDateTime meetAt = request.meetAt() != null ? request.meetAt() : gathering.getMeetAt();

        GatheringCategory parsedCategory = parseCategory(request.category());
        GatheringCategory category = parsedCategory != null ? parsedCategory : gathering.getCategory();

        short maxMembers = gathering.getMaxMembers();
        if (request.maxMembers() != null) {
            if (request.maxMembers() < gathering.getCurrentMembers()) {
                // 현재 참여 인원보다 적게 줄일 수 없음
                throw new BusinessException(ErrorCode.INVALID_REQUEST);
            }
            maxMembers = request.maxMembers().shortValue();
        }

        gathering.update(title, content, category, location, maxMembers, fusionEnabled, meetAt);

        // 태그/이미지: 목록이 오면 전체 교체(availability와 동일 패턴)
        if (request.tagIds() != null) {
            gatheringTagRepository.deleteAllByGatheringId(gatheringId);
            saveTags(gathering, request.tagIds());
        }
        if (request.imageUrls() != null) {
            gatheringImageRepository.deleteAllByGatheringId(gatheringId);
            saveImages(gathering, request.imageUrls());
        }

        return new GatheringUpdateResponse(gathering.getId(), gathering.getUpdatedAt());
    }

    /**
     * 모임 취소(소프트).
     *
     * 방장·모집중(RECRUITING) 조건을 검증한 뒤 status = CANCELED 로 전이하고
     * canceled_at 을 기록한다(물리 삭제 아님).
     */
    @Transactional
    public GatheringCancelResponse cancel(
            String authUserId,
            Long gatheringId
    ) {
        User me = userRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GATHERING_NOT_FOUND));

        if (!gathering.isHost(me.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (!gathering.isRecruiting()) {
            throw new BusinessException(ErrorCode.GATHERING_NOT_MODIFIABLE);
        }

        gathering.cancel();

        return new GatheringCancelResponse(
                gathering.getId(),
                gathering.getStatus(),
                gathering.getCanceledAt()
        );
    }

    @Transactional
    public GatheringCreateResponse create(
            String authUserId,
            GatheringCreateRequest request
    ) {
        User host = userRepository
                .findByAuthUserId(authUserId)
                .orElseThrow(
                        () -> new BusinessException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );

        Gathering gathering = new Gathering(
                host,
                request.title(),
                request.content(),
                request.category(),
                request.location(),
                (short) request.maxMembers(),
                request.fusionEnabled(),
                request.recruitStartAt(),
                request.recruitEndAt(),
                request.meetAt()
        );
        gatheringRepository.save(gathering);

        // 방장을 HOST 멤버로 등록 (current_members는 엔티티 생성 시 1로 시작)
        gatheringMemberRepository.save(
                GatheringMember.host(gathering, host)
        );

        saveTags(gathering, request.tagIds());
        saveImages(gathering, request.imageUrls());

        return GatheringCreateResponse.from(gathering);
    }

    private void saveTags(
            Gathering gathering,
            List<Long> tagIds
    ) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }

        List<Long> distinctTagIds = tagIds.stream()
                .distinct()
                .toList();

        long foundCount = tagRepository
                .findAllById(distinctTagIds)
                .size();

        if (foundCount != distinctTagIds.size()) {
            throw new BusinessException(ErrorCode.INVALID_TAG);
        }

        List<GatheringTag> gatheringTags = distinctTagIds.stream()
                .map(tagId -> new GatheringTag(gathering, tagId))
                .toList();

        gatheringTagRepository.saveAll(gatheringTags);
    }

    private void saveImages(
            Gathering gathering,
            List<String> imageUrls
    ) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }

        List<GatheringImage> images = new ArrayList<>();
        short sortOrder = 0;
        for (String imageUrl : imageUrls) {
            images.add(
                    new GatheringImage(gathering, imageUrl, sortOrder)
            );
            sortOrder++;
        }

        gatheringImageRepository.saveAll(images);
    }
}
