package com.twogether.backend.user.controller;

import com.twogether.backend.global.response.ApiResponse;
import com.twogether.backend.user.dto.request.OnboardingUpdateRequest;
import com.twogether.backend.user.dto.response.MyProfileResponse;
import com.twogether.backend.user.dto.response.NicknameCheckResponse;
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

@Tag(
        name = "사용자 API",
        description = "사용자 프로필 및 온보딩 관련 API"
)
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Operation(
            summary = "내 온보딩 프로필 수정",
            description = """
                    학교 이메일 인증 후 사용자의 기본 프로필을 저장합니다.

                    Supabase 로그인 후 발급받은 access token을
                    Authorization 헤더에 Bearer 형식으로 전달해야 합니다.

                    캠퍼스 정보는 프론트에서 전달하지 않으며,
                    실제 구현에서는 학번을 기준으로 백엔드가 자동 분류합니다.

                    취미 태그, 기술 태그, 가용 일정은
                    기본 프로필 완료 후 사용자가 별도로 설정할 수 있습니다.

                    현재 Swagger 명세 단계에서는 실제 DB에 저장하지 않고
                    성공 응답만 반환합니다.
                    """
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PatchMapping("/me/onboarding")
    public ResponseEntity<ApiResponse<Void>> updateMyOnboarding(
            @RequestBody OnboardingUpdateRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("온보딩 프로필 수정에 성공했습니다.")
        );
    }

    @Operation(
            summary = "내 프로필 조회",
            description = """
                    현재 로그인한 사용자의 기본 프로필을 조회합니다.

                    Supabase 로그인 후 발급받은 access token을
                    Authorization 헤더에 Bearer 형식으로 전달해야 합니다.

                    프론트 화면 이동 기준:
                    - emailVerified가 false이면 학교 이메일 인증 화면으로 이동합니다.
                    - emailVerified가 true이고 profileCompleted가 false이면 기본 프로필 입력 화면으로 이동합니다.
                    - emailVerified와 profileCompleted가 모두 true이면 메인 화면으로 이동할 수 있습니다.

                    취미 태그, 기술 태그, 가용 일정은
                    기본 프로필 완료 후 사용자가 별도로 설정할 수 있습니다.

                    departmentName은 화면 표시용으로 사용합니다.
                    departmentId는 프로필 수정 화면에서 기존 학과 선택값을 표시할 때 사용합니다.

                    campus는 학번을 기준으로 백엔드가 분류한 캠퍼스입니다.
                    preferredRegion은 사용자가 선택한 시·군·구 단위 선호 활동 지역입니다.

                    현재 Swagger 명세 단계에서는 토큰을 실제 검증하지 않고
                    더미 사용자 정보를 반환합니다.
                    """
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MyProfileResponse>> getMyProfile() {

        MyProfileResponse profile = new MyProfileResponse(
                1L,
                "최인준",
                "인준",
                24,
                "60231234",
                1L,
                "컴퓨터공학과",
                "NATURAL",
                "서울 강남구",
                true,
                true
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "내 프로필 조회에 성공했습니다.",
                        profile
                )
        );
    }

    @Operation(
            summary = "닉네임 중복 확인",
            description = """
                    사용자가 입력한 닉네임의 사용 가능 여부를 확인합니다.

                    available이 true이면 사용할 수 있는 닉네임이고,
                    false이면 이미 사용 중인 닉네임입니다.

                    프론트에서는 닉네임 입력 후 중복 확인 버튼을 눌렀을 때
                    이 API를 호출할 수 있습니다.

                    현재 Swagger 명세 단계에서는
                    '관리자'라는 닉네임만 중복으로 처리합니다.
                    """
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/nickname/check")
    public ResponseEntity<ApiResponse<NicknameCheckResponse>> checkNickname(
            @RequestParam String nickname
    ) {
        boolean available = !nickname.equals("관리자");

        NicknameCheckResponse response =
                new NicknameCheckResponse(
                        available,
                        nickname
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