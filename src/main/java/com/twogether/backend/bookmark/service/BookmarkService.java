package com.twogether.backend.bookmark.service;

import com.twogether.backend.bookmark.domain.GatheringBookmark;
import com.twogether.backend.bookmark.domain.UserBookmark;
import com.twogether.backend.bookmark.dto.response.BookmarkStateResponse;
import com.twogether.backend.bookmark.dto.response.BookmarkedUserResponse;
import com.twogether.backend.bookmark.dto.response.MyBookmarksResponse;
import com.twogether.backend.bookmark.repository.GatheringBookmarkRepository;
import com.twogether.backend.bookmark.repository.UserBookmarkRepository;
import com.twogether.backend.department.domain.Department;
import com.twogether.backend.department.repository.DepartmentRepository;
import com.twogether.backend.gathering.domain.Gathering;
import com.twogether.backend.gathering.dto.response.GatheringSummaryResponse;
import com.twogether.backend.gathering.repository.GatheringMemberRepository;
import com.twogether.backend.gathering.repository.GatheringRepository;
import com.twogether.backend.gathering.repository.GatheringTagName;
import com.twogether.backend.gathering.repository.GatheringTagRepository;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.tag.dto.response.UserTagsResponse;
import com.twogether.backend.tag.service.TagService;
import com.twogether.backend.user.domain.User;
import com.twogether.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class BookmarkService {

    private final UserRepository userRepository;
    private final UserBookmarkRepository userBookmarkRepository;
    private final GatheringRepository gatheringRepository;
    private final GatheringBookmarkRepository gatheringBookmarkRepository;
    private final GatheringMemberRepository gatheringMemberRepository;
    private final GatheringTagRepository gatheringTagRepository;
    private final DepartmentRepository departmentRepository;
    private final TagService tagService;

    public BookmarkService(
            UserRepository userRepository,
            UserBookmarkRepository userBookmarkRepository,
            GatheringRepository gatheringRepository,
            GatheringBookmarkRepository gatheringBookmarkRepository,
            GatheringMemberRepository gatheringMemberRepository,
            GatheringTagRepository gatheringTagRepository,
            DepartmentRepository departmentRepository,
            TagService tagService
    ) {
        this.userRepository = userRepository;
        this.userBookmarkRepository = userBookmarkRepository;
        this.gatheringRepository = gatheringRepository;
        this.gatheringBookmarkRepository = gatheringBookmarkRepository;
        this.gatheringMemberRepository = gatheringMemberRepository;
        this.gatheringTagRepository = gatheringTagRepository;
        this.departmentRepository = departmentRepository;
        this.tagService = tagService;
    }

    @Transactional
    public BookmarkStateResponse toggleUserBookmark(
            String authUserId,
            Long targetUserId
    ) {
        User me = findUser(authUserId);
        if (Objects.equals(me.getId(), targetUserId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return userBookmarkRepository.findByBookmarkerIdAndBookmarkedUserId(me.getId(), target.getId())
                .map(existing -> {
                    userBookmarkRepository.delete(existing);
                    return new BookmarkStateResponse(false);
                })
                .orElseGet(() -> {
                    userBookmarkRepository.save(new UserBookmark(me, target));
                    return new BookmarkStateResponse(true);
                });
    }

    @Transactional
    public BookmarkStateResponse toggleGatheringBookmark(
            String authUserId,
            Long gatheringId
    ) {
        User me = findUser(authUserId);
        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GATHERING_NOT_FOUND));

        return gatheringBookmarkRepository.findByBookmarkerIdAndGatheringId(me.getId(), gathering.getId())
                .map(existing -> {
                    gatheringBookmarkRepository.delete(existing);
                    return new BookmarkStateResponse(false);
                })
                .orElseGet(() -> {
                    gatheringBookmarkRepository.save(new GatheringBookmark(me, gathering));
                    return new BookmarkStateResponse(true);
                });
    }

    public boolean isUserBookmarked(
            String authUserId,
            Long targetUserId
    ) {
        if (authUserId == null) {
            return false;
        }

        return userRepository.findByAuthUserId(authUserId)
                .map(me -> userBookmarkRepository.existsByBookmarkerIdAndBookmarkedUserId(me.getId(), targetUserId))
                .orElse(false);
    }

    public boolean isGatheringBookmarked(
            String authUserId,
            Long gatheringId
    ) {
        if (authUserId == null) {
            return false;
        }

        return userRepository.findByAuthUserId(authUserId)
                .map(me -> gatheringBookmarkRepository.existsByBookmarkerIdAndGatheringId(me.getId(), gatheringId))
                .orElse(false);
    }

    public MyBookmarksResponse getMyBookmarks(
            String authUserId
    ) {
        User me = findUser(authUserId);

        List<BookmarkedUserResponse> users = userBookmarkRepository.findByBookmarkerIdOrderByCreatedAtDesc(me.getId()).stream()
                .map(bookmark -> toBookmarkedUserResponse(bookmark.getBookmarkedUser()))
                .filter(Objects::nonNull)
                .toList();

        List<Gathering> gatherings = gatheringBookmarkRepository.findAllByBookmarkerIdWithGathering(me.getId()).stream()
                .map(GatheringBookmark::getGathering)
                .toList();

        Map<Long, List<String>> tagsByGathering = loadTagsByGathering(gatherings);
        Map<Long, String> departmentNameById = loadHostDepartmentNames(gatherings);
        Map<Long, Integer> memberCountByGathering = loadMemberCounts(gatherings);

        OffsetDateTime now = OffsetDateTime.now();
        List<GatheringSummaryResponse> bookmarkedGatherings = gatherings.stream()
                .map(gathering -> GatheringSummaryResponse.of(
                        gathering,
                        memberCountByGathering.getOrDefault(gathering.getId(), 0),
                        tagsByGathering.getOrDefault(gathering.getId(), List.of()),
                        departmentNameById.get(gathering.getHost().getDepartmentId()),
                        now
                ))
                .toList();

        return new MyBookmarksResponse(users, bookmarkedGatherings);
    }

    private BookmarkedUserResponse toBookmarkedUserResponse(User target) {
        Department department = target.getDepartmentId() != null
                ? departmentRepository.findById(target.getDepartmentId()).orElse(null)
                : null;

        String campus = department != null && department.getCollege() != null && department.getCollege().getCampus() != null
                ? department.getCollege().getCampus().name()
                : null;

        UserTagsResponse tags = tagService.getUserTags(target.getId());

        return new BookmarkedUserResponse(
                target.getId(),
                target.getNickname(),
                target.getAge(),
                department != null ? department.getName() : null,
                campus,
                target.getPreferredRegion(),
                target.getProfileImageUrl(),
                target.getIntroduction(),
                tags.hobbyTags(),
                tags.skillTags()
        );
    }

    private Map<Long, List<String>> loadTagsByGathering(List<Gathering> gatherings) {
        if (gatherings.isEmpty()) {
            return Map.of();
        }

        List<Long> gatheringIds = gatherings.stream().map(Gathering::getId).toList();

        return gatheringTagRepository.findTagNamesByGatheringIds(gatheringIds).stream()
                .collect(Collectors.groupingBy(
                        GatheringTagName::getGatheringId,
                        Collectors.mapping(GatheringTagName::getTagName, Collectors.toList())
                ));
    }

    private Map<Long, String> loadHostDepartmentNames(List<Gathering> gatherings) {
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

    private Map<Long, Integer> loadMemberCounts(List<Gathering> gatherings) {
        if (gatherings.isEmpty()) {
            return Map.of();
        }

        List<Long> gatheringIds = gatherings.stream().map(Gathering::getId).toList();

        return gatheringMemberRepository.countMembersByGatheringIds(gatheringIds).stream()
                .collect(Collectors.toMap(
                        GatheringMemberRepository.GatheringMemberCount::getGatheringId,
                        result -> Math.toIntExact(result.getMemberCount())
                ));
    }

    private User findUser(String authUserId) {
        return userRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}