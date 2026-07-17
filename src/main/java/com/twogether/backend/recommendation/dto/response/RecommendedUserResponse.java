package com.twogether.backend.recommendation.dto.response;

import com.twogether.backend.recommendation.dto.request.AiTagInfo;
import com.twogether.backend.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "AI 팀원 추천 사용자 응답")
public record RecommendedUserResponse(

        @Schema(
                description = "추천 사용자 ID",
                example = "14"
        )
        Long userId,

        @Schema(
                description = "추천 사용자 닉네임",
                example = "인준"
        )
        String nickname,

        @Schema(
                description = "프로필 이미지 URL",
                example = "https://cdn.example.com/profile.png"
        )
        String profileImageUrl,

        @Schema(
                description = "학과 ID",
                example = "3"
        )
        Long departmentId,

        @Schema(
                description = "사용자 자기소개",
                example = "백엔드 개발과 해커톤에 관심이 있습니다."
        )
        String introduction,

        @Schema(description = "사용자 태그 목록")
        List<AiTagInfo> tags,

        @Schema(
                description = "AI 추천 점수",
                example = "91"
        )
        int score,

        @Schema(
                description = "AI 추천 이유",
                example = "Spring Boot 기술 태그와 모임 목적이 잘 맞습니다."
        )
        String reason

) {

    public static RecommendedUserResponse of(
            User user,
            List<AiTagInfo> tags,
            AiRecommendedCandidate recommendation
    ) {
        return new RecommendedUserResponse(
                user.getId(),
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getDepartmentId(),
                user.getIntroduction(),
                tags == null ? List.of() : List.copyOf(tags),
                recommendation.score(),
                recommendation.reason()
        );
    }
}