package com.twogether.backend.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 온보딩 프로필 수정 요청")
public record OnboardingUpdateRequest(

        @Schema(
                description = "사용자 실명",
                example = "최인준"
        )
        String realName,

        @Schema(
                description = "서비스에서 사용할 닉네임",
                example = "인준"
        )
        String nickname,

        @Schema(
                description = "사용자 나이",
                example = "23"
        )
        Integer age,

        @Schema(
                description = "\"명지대학교 학번. 사용자 정보로 저장되며 캠퍼스 분류에는 사용하지 않습니다.\"",
                example = "60231234"
        )
        String studentNumber,

        @Schema(
                description = "선택한 학과의 고유 ID",
                example = "1"
        )
        Long departmentId,

        @Schema(
                description = "사용자가 모임 활동을 선호하는 시·군·구 단위 지역",
                example = "서울 강남구"
        )
        String preferredRegion

) {
}