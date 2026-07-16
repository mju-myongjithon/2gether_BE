package com.twogether.backend.user.dto.response;

import com.twogether.backend.tag.dto.response.HobbyTagResponse;
import com.twogether.backend.tag.dto.response.SkillTagResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "타 사용자 공개 프로필 조회 응답")
public record UserProfileResponse(

        @Schema(
                description = "사용자 고유 ID",
                example = "2"
        )
        Long id,

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
                description = "소속 캠퍼스",
                example = "NATURAL"
        )
        String campus,

        @Schema(
                description = "선호 활동 지역",
                example = "서울"
        )
        String preferredRegion,

        @Schema(
                description = "자기소개",
                example = "백엔드 개발을 공부하고 있습니다."
        )
        String introduction,

        @Schema(
                description = "프로필 이미지 URL",
                example = "https://example.com/profile.jpg"
        )
        String profileImageUrl,

        @Schema(description = "관심사 태그 목록")
        List<HobbyTagResponse> hobbyTags,

        @Schema(description = "스킬 태그 목록")
        List<SkillTagResponse> skillTags
) {
}