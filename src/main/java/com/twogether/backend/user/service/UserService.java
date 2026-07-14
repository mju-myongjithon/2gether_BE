package com.twogether.backend.user.service;

import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.user.domain.User;
import com.twogether.backend.user.dto.request.OnboardingUpdateRequest;
import com.twogether.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.twogether.backend.user.dto.response.MyProfileResponse;

import java.util.regex.Pattern;

@Service
@Transactional(readOnly = true)
public class UserService {

    private static final int NICKNAME_MIN_LENGTH = 2;
    private static final int NICKNAME_MAX_LENGTH = 12;

    private static final Pattern NICKNAME_PATTERN =
            Pattern.compile("^[가-힣a-zA-Z0-9_]+$");

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Supabase 사용자 ID를 기준으로 회원을 조회합니다.
     * 등록된 회원이 없으면 기본 상태의 신규 회원을 생성합니다.
     */
    @Transactional
    public User findOrCreateUser(String authUserId) {
        return userRepository.findByAuthUserId(authUserId)
                .orElseGet(() ->
                        userRepository.save(new User(authUserId))
                );
    }

    /**
     * 닉네임의 앞뒤 공백을 제거한 뒤 형식을 검증합니다.
     */
    public String validateAndNormalizeNickname(String nickname) {

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

        if (normalizedNickname.length() < NICKNAME_MIN_LENGTH
                || normalizedNickname.length() > NICKNAME_MAX_LENGTH) {

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
    public boolean isNicknameAvailable(String nickname) {
        String normalizedNickname =
                validateAndNormalizeNickname(nickname);

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
        User user = userRepository.findByAuthUserId(authUserId)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );

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

        if (nicknameAlreadyExists && nicknameChanged) {
            throw new BusinessException(
                    ErrorCode.DUPLICATE_NICKNAME
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
     * 현재 로그인한 사용자의 프로필을 조회합니다.
     * 회원이 없으면 기본 상태의 신규 회원을 생성합니다.
     */
    @Transactional
    public MyProfileResponse getMyProfile(String authUserId) {

        User user = findOrCreateUser(authUserId);

        return new MyProfileResponse(
                user.getId(),
                user.getRealName(),
                user.getNickname(),
                user.getAge(),
                user.getStudentNumber(),
                user.getDepartmentId(),

                // 아직 Department 엔티티와 연결하지 않았으므로 임시 null
                null,

                // 아직 캠퍼스 정보를 계산하지 않으므로 임시 null
                null,

                user.getPreferredRegion(),
                user.getProfileImageUrl(),
                user.isEmailVerified(),
                user.isProfileCompleted()
        );
    }
}