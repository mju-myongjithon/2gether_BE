package com.twogether.backend.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 프로필 조회 응답")
public record MyProfileResponse(

        @Schema(
                description = "사용자 고유 ID",
                example = "1"
        )
        Long id,

        @Schema(
                description = "사용자 실명",
                example = "최인준"
        )
        String realName,

        @Schema(
                description = "서비스 닉네임",
                example = "인준"
        )
        String nickname,

        @Schema(
                description = "사용자 나이",
                example = "24"
        )
        Integer age,

        @Schema(
                description = "명지대학교 학번",
                example = "60231234"
        )
        String studentNumber,

        @Schema(
                description = "소속 학과 ID",
                example = "1"
        )
        Long departmentId,

        @Schema(
                description = "소속 학과명",
                example = "컴퓨터공학과"
        )
        String departmentName,

        @Schema(
                description = "학번을 기준으로 백엔드가 분류한 캠퍼스",
                example = "NATURAL"
        )
        String campus,

        @Schema(
                description = "사용자가 선택한 시·군·구 단위 선호 활동 지역",
                example = "서울 강남구"
        )
        String preferredRegion,

        @Schema(
                description = """
                        학교 이메일 인증 완료 여부입니다.
                        false이면 프론트는 학교 이메일 인증 화면을 표시합니다.
                        """,
                example = "true"
        )
        boolean emailVerified,

        @Schema(
                description = """
                        기본 프로필 작성 완료 여부입니다.
                        취미 태그, 기술 태그, 가용 일정 설정 완료 여부는 포함하지 않습니다.
                        """,
                example = "true"
        )
        boolean profileCompleted

) {
}