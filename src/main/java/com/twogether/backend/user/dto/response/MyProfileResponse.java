package com.twogether.backend.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 프로필 및 가입 상태 조회 응답")
public record MyProfileResponse(

        @Schema(
                description = "사용자 고유 ID",
                example = "1"
        )
        Long id,

        @Schema(
                description = "사용자 실명",
                example = "최인준",
                nullable = true
        )
        String realName,

        @Schema(
                description = "서비스 닉네임",
                example = "인준",
                nullable = true
        )
        String nickname,

        @Schema(
                description = "사용자 나이",
                example = "23",
                nullable = true
        )
        Integer age,

        @Schema(
                description = "명지대학교 학번",
                example = "60231234",
                nullable = true
        )
        String studentNumber,

        @Schema(
                description = "소속 학과 ID",
                example = "1",
                nullable = true
        )
        Long departmentId,

        @Schema(
                description = "사용자가 선택한 소속 학과명",
                example = "컴퓨터공학과",
                nullable = true
        )
        String departmentName,

        @Schema(
                description = "선택한 학과가 소속된 캠퍼스",
                example = "NATURAL",
                allowableValues = {
                        "HUMANITIES",
                        "NATURAL"
                },
                nullable = true
        )
        String campus,

        @Schema(
                description = "사용자가 선택한 시·군·구 단위 선호 활동 지역",
                example = "서울 강남구",
                nullable = true
        )
        String preferredRegion,

        @Schema(
                description = """
                        사용자의 간단한 자기소개입니다.
                        자기소개를 작성하지 않은 경우 null로 반환됩니다.
                        """,
                example = "백엔드 개발과 운동을 좋아합니다.",
                nullable = true
        )
        String introduction,

        @Schema(
                description = """
                        현재 설정된 프로필 이미지 URL입니다.
                        프로필 이미지를 설정하지 않은 경우 null로 반환됩니다.
                        """,
                example = "https://example.com/profile-images/user-1.jpg",
                nullable = true
        )
        String profileImageUrl,

        @Schema(
                description = """
                        학교 이메일 인증 완료 여부입니다.
                        false이면 프론트는 학교 이메일 인증 화면으로 이동합니다.
                        """,
                example = "false"
        )
        boolean emailVerified,

        @Schema(
                description = """
                        필수 온보딩 프로필 작성 완료 여부입니다.
                        false이면 프론트는 온보딩 화면으로 이동합니다.
                        """,
                example = "true"
        )
        boolean profileCompleted

) {
}