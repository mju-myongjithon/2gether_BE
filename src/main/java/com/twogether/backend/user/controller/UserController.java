package com.twogether.backend.user.controller;

import com.twogether.backend.global.response.ApiResponse;
import com.twogether.backend.user.dto.request.OnboardingUpdateRequest;
import com.twogether.backend.user.dto.request.ProfileImageUpdateRequest;
import com.twogether.backend.user.dto.response.MyProfileResponse;
import com.twogether.backend.user.dto.response.NicknameCheckResponse;
import com.twogether.backend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

@Tag(
        name = "사용자 API",
        description = "사용자 프로필 및 온보딩 관련 API"
)
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
            summary = "내 온보딩 프로필 수정",
            description = """
                Supabase 로그인 후 발급받은 access token을
                Authorization 헤더에 Bearer 형식으로 전달합니다.

                토큰에서 현재 로그인 사용자를 식별하고
                사용자의 기본 프로필을 저장합니다.
                """
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PatchMapping("/me/onboarding")
    public ResponseEntity<?> completeOnboarding(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody OnboardingUpdateRequest request
    ) {
        String authUserId = jwt.getSubject();

        userService.findOrCreateUser(authUserId);

        return ResponseEntity.ok(
                userService.completeProfile(authUserId, request)
        );
    }

    @Operation(
            summary = "내 프로필 및 가입 상태 조회",
            description = """
                Supabase 로그인 후 발급받은 access token을
                Authorization 헤더에 Bearer 형식으로 전달합니다.

                백엔드는 토큰의 sub 값을 기준으로 현재 사용자를 식별합니다.

                회원이 존재하지 않으면 기본 상태의 회원을 최초 생성하고,
                실제 프로필 정보와 가입 진행 상태를 반환합니다.

                프론트는 emailVerified와 profileCompleted 값을 기준으로
                학교 이메일 인증 화면, 온보딩 화면, 메인 화면으로 이동합니다.
                """
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MyProfileResponse>> getMyProfile(
            @AuthenticationPrincipal Jwt jwt
    ) {
        String authUserId = jwt.getSubject();

        MyProfileResponse profile =
                userService.getMyProfile(authUserId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "내 프로필 조회에 성공했습니다.",
                        profile
                )
        );
    }

    @Operation(
            summary = "내 프로필 이미지 수정",
            description = """
                    현재 로그인한 사용자의 프로필 이미지를 수정합니다.

                    프론트에서 이미지 파일을 Supabase Storage에 먼저 업로드한 뒤,
                    발급받은 이미지 URL을 이 API로 전달합니다.

                    현재는 실제 DB 저장 기능을 연결하기 전이므로
                    성공 응답만 반환합니다.
                    """
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PatchMapping("/me/profile-image")
    public ResponseEntity<ApiResponse<Void>> updateMyProfileImage(
            @RequestBody ProfileImageUpdateRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "프로필 이미지 수정에 성공했습니다."
                )
        );
    }

    @Operation(
            summary = "닉네임 중복 확인",
            description = """
                    사용자가 입력한 닉네임의 형식과 중복 여부를 확인합니다.

                    닉네임 규칙:
                    - 앞뒤 공백 제거
                    - 2자 이상 12자 이하
                    - 한글, 영문, 숫자, 밑줄만 사용 가능

                    available이 true이면 사용할 수 있는 닉네임이고,
                    false이면 이미 사용 중인 닉네임입니다.
                    """
    )
    @GetMapping("/nickname/check")
    public ResponseEntity<ApiResponse<NicknameCheckResponse>> checkNickname(
            @RequestParam String nickname
    ) {
        String normalizedNickname =
                userService.validateAndNormalizeNickname(
                        nickname
                );

        boolean available =
                userService.isNicknameAvailable(
                        normalizedNickname
                );

        NicknameCheckResponse response =
                new NicknameCheckResponse(
                        available,
                        normalizedNickname
                );

        String message = available
                ? "사용 가능한 닉네임입니다."
                : "이미 사용 중인 닉네임입니다.";

        return ResponseEntity.ok(
                ApiResponse.success(
                        message,
                        response
                )
        );
    }
}