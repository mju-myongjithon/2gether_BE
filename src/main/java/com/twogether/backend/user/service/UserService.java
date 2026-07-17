package com.twogether.backend.user.service;

import com.twogether.backend.availability.repository.AvailabilityRepository;
import com.twogether.backend.bookmark.repository.UserBookmarkRepository;
import com.twogether.backend.department.domain.Department;
import com.twogether.backend.gathering.repository.GatheringMemberRepository;
import com.twogether.backend.department.repository.DepartmentRepository;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.tag.repository.UserTagRepository;
import com.twogether.backend.user.domain.User;
import com.twogether.backend.user.dto.request.IntroductionUpdateRequest;
import com.twogether.backend.user.dto.request.OnboardingUpdateRequest;
import com.twogether.backend.user.dto.request.ProfileImageUpdateRequest;
import com.twogether.backend.user.dto.response.MyProfileResponse;
import com.twogether.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.twogether.backend.tag.dto.response.UserTagsResponse;
import com.twogether.backend.tag.service.TagService;
import com.twogether.backend.user.dto.response.UserProfileResponse;

import java.util.regex.Pattern;

@Service
@Transactional(readOnly = true)
public class UserService {

    private static final int NICKNAME_MIN_LENGTH = 2;
    private static final int NICKNAME_MAX_LENGTH = 12;
    private static final int INTRODUCTION_MAX_LENGTH = 200;

    private static final Pattern NICKNAME_PATTERN =
            Pattern.compile("^[가-힣a-zA-Z0-9_]+$");

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final AvailabilityRepository availabilityRepository;
    private final UserTagRepository userTagRepository;
    private final UserBookmarkRepository userBookmarkRepository;
    private final GatheringMemberRepository gatheringMemberRepository;
    private final TagService tagService;
    public UserService(
            UserRepository userRepository,
            DepartmentRepository departmentRepository,
            AvailabilityRepository availabilityRepository,
            UserTagRepository userTagRepository,
            UserBookmarkRepository userBookmarkRepository,
            GatheringMemberRepository gatheringMemberRepository,
            TagService tagService
    ) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.availabilityRepository = availabilityRepository;
        this.userTagRepository = userTagRepository;
        this.userBookmarkRepository = userBookmarkRepository;
        this.gatheringMemberRepository = gatheringMemberRepository;
        this.tagService = tagService;
    }

    /**
     * Supabase 사용자 ID를 기준으로 회원을 조회합니다.
     * 등록된 회원이 없으면 기본 상태의 신규 회원을 생성합니다.
     */
    @Transactional
    public User findOrCreateUser(
            String authUserId
    ) {
        return userRepository.findByAuthUserId(authUserId)
                .orElseGet(() ->
                        userRepository.save(
                                new User(authUserId)
                        )
                );
    }

    /**
     * 닉네임의 앞뒤 공백을 제거한 뒤 형식을 검증합니다.
     */
    public String validateAndNormalizeNickname(
            String nickname
    ) {
        if (nickname == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_NICKNAME_EMPTY
            );
        }

        String normalizedNickname = nickname.trim();

        if (normalizedNickname.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.INVALID_NICKNAME_EMPTY
            );
        }

        if (normalizedNickname.length()
                < NICKNAME_MIN_LENGTH
                || normalizedNickname.length()
                > NICKNAME_MAX_LENGTH) {

            throw new BusinessException(
                    ErrorCode.INVALID_NICKNAME_LENGTH
            );
        }

        if (!NICKNAME_PATTERN
                .matcher(normalizedNickname)
                .matches()) {

            throw new BusinessException(
                    ErrorCode.INVALID_NICKNAME_FORMAT
            );
        }

        return normalizedNickname;
    }

    /**
     * 닉네임의 형식을 검증하고 DB 중복 여부를 확인합니다.
     *
     * true: 사용 가능
     * false: 이미 사용 중
     */
    public boolean isNicknameAvailable(
            String nickname
    ) {
        String normalizedNickname =
                validateAndNormalizeNickname(
                        nickname
                );

        return !userRepository.existsByNickname(
                normalizedNickname
        );
    }

    /**
     * 사용자의 온보딩 프로필을 저장합니다.
     */
    @Transactional
    public User completeProfile(
            String authUserId,
            OnboardingUpdateRequest request
    ) {
        User user = findUser(authUserId);

        String normalizedNickname =
                validateAndNormalizeNickname(
                        request.nickname()
                );

        boolean nicknameAlreadyExists =
                userRepository.existsByNickname(
                        normalizedNickname
                );

        boolean nicknameChanged =
                user.getNickname() == null
                        || !user.getNickname()
                        .equals(normalizedNickname);

        if (nicknameAlreadyExists
                && nicknameChanged) {

            throw new BusinessException(
                    ErrorCode.DUPLICATE_NICKNAME
            );
        }

        boolean departmentExists =
                request.departmentId() != null
                        && departmentRepository.existsById(
                        request.departmentId()
                );

        if (!departmentExists) {
            throw new BusinessException(
                    ErrorCode.DEPARTMENT_NOT_FOUND
            );
        }

        user.completeProfile(
                request.realName(),
                normalizedNickname,
                request.age(),
                request.studentNumber(),
                request.departmentId(),
                request.preferredRegion()
        );

        return user;
    }

    /**
     * 현재 로그인한 사용자의 프로필 이미지를 수정합니다.
     *
     * null을 전달하면 기존 프로필 이미지를 제거합니다.
     */
    @Transactional
    public void updateProfileImage(
            String authUserId,
            ProfileImageUpdateRequest request
    ) {
        User user = findUser(authUserId);

        user.updateProfileImage(
                request.profileImageUrl()
        );
    }

    /**
     * 현재 로그인한 사용자의 간단한 자기소개를 수정합니다.
     *
     * null 또는 공백만 전달하면 기존 자기소개를 제거합니다.
     */
    @Transactional
    public void updateIntroduction(
            String authUserId,
            IntroductionUpdateRequest request
    ) {
        User user = findUser(authUserId);

        String normalizedIntroduction =
                normalizeIntroduction(
                        request.introduction()
                );

        user.updateIntroduction(
                normalizedIntroduction
        );
    }

    /**
     * 현재 로그인한 사용자의 프로필을 조회합니다.
     * 회원이 없으면 기본 상태의 신규 회원을 생성합니다.
     */
    @Transactional
    public MyProfileResponse getMyProfile(
            String authUserId
    ) {
        User user = findOrCreateUser(authUserId);

        String departmentName = null;
        String campus = null;

        if (user.getDepartmentId() != null) {
            Department department =
                    departmentRepository
                            .findById(user.getDepartmentId())
                            .orElse(null);

            if (department != null) {
                departmentName =
                        department.getName();

                campus =
                        department.getCollege()
                                .getCampus()
                                .name();
            }
        }

        return new MyProfileResponse(
                user.getId(),
                user.getRealName(),
                user.getNickname(),
                user.getAge(),
                user.getStudentNumber(),
                user.getDepartmentId(),
                departmentName,
                campus,
                user.getPreferredRegion(),
                user.getIntroduction(),
                user.getProfileImageUrl(),
                user.isEmailVerified(),
                user.isProfileCompleted()
        );
    }

    /**
     * 현재 로그인한 사용자의 서비스 데이터를 삭제합니다.
     *
     * 이번 구현에서는 백엔드 DB 데이터만 삭제하며,
     * Supabase Auth 계정은 삭제하지 않습니다.
     */
    @Transactional
    public void withdraw(
            String authUserId
    ) {
        User user = findUser(authUserId);

        availabilityRepository
                .deleteAllByUserAuthUserId(
                        authUserId
                );

        userTagRepository
                .deleteAllByUser_Id(
                        user.getId()
                );

        userBookmarkRepository.deleteAllByBookmarkerIdOrBookmarkedUserId(
                user.getId(),
                user.getId()
        );

        userRepository.delete(user);
    }

    /**
     * Supabase 사용자 ID를 기준으로 사용자를 조회합니다.
     */
    private User findUser(
            String authUserId
    ) {
        return userRepository
                .findByAuthUserId(authUserId)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );
    }

    /**
     * 자기소개 앞뒤 공백을 제거하고 길이를 검증합니다.
     *
     * null 또는 공백만 입력하면 null로 변환합니다.
     */
    private String normalizeIntroduction(
            String introduction
    ) {
        if (introduction == null) {
            return null;
        }

        String normalizedIntroduction =
                introduction.trim();

        if (normalizedIntroduction.isEmpty()) {
            return null;
        }

        if (normalizedIntroduction.length()
                > INTRODUCTION_MAX_LENGTH) {

            throw new BusinessException(
                    ErrorCode.INVALID_INTRODUCTION_LENGTH
            );
        }

        return normalizedIntroduction;
    }

    /**
     * 사용자 ID를 기준으로 다른 사용자의 공개 프로필과
     * 관심사/스킬 태그를 함께 조회합니다.
     */
    public UserProfileResponse getUserProfile(
            Long userId,
            String authUserId
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );

        String departmentName = null;
        String campus = null;

        if (user.getDepartmentId() != null) {
            Department department =
                    departmentRepository
                            .findById(user.getDepartmentId())
                            .orElse(null);

            if (department != null) {
                departmentName =
                        department.getName();

                campus =
                        department.getCollege()
                                .getCampus()
                                .name();
            }
        }

        UserTagsResponse tags =
                tagService.getUserTags(userId);

        boolean bookmarked = false;
        if (authUserId != null) {
            bookmarked = userRepository.findByAuthUserId(authUserId)
                    .map(me -> userBookmarkRepository.existsByBookmarkerIdAndBookmarkedUserId(me.getId(), userId))
                    .orElse(false);
        }

        Long participatingGatheringsCount = gatheringMemberRepository.countByUserId(userId);

        return new UserProfileResponse(
                user.getId(),
                user.getNickname(),
                user.getAge(),
                user.getDepartmentId(),
                departmentName,
                campus,
                user.getPreferredRegion(),
                user.getIntroduction(),
                user.getProfileImageUrl(),
                tags.hobbyTags(),
<<<<<<< HEAD
                tags.skillTags(),
                participatingGatheringsCount,
                bookmarked
=======
                                tags.skillTags(),
                                bookmarked
>>>>>>> 41793dc568af2a19c3d7a1504deaf2e5ccac4ae3
        );
    }
}