package com.twogether.backend.recommendation.dto.request;

import com.twogether.backend.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "AI 추천에 전달할 후보 사용자 정보")
public record AiCandidateInfo(

        @Schema(
                description = "사용자 고유 ID",
                example = "14"
        )
        Long userId,

        @Schema(
                description = "사용자 닉네임",
                example = "인준"
        )
        String nickname,

        @Schema(
                description = "소속 학과 ID",
                example = "3"
        )
        Long departmentId,

        @Schema(
                description = "선호 지역",
                example = "자연캠"
        )
        String preferredRegion,

        @Schema(
                description = "사용자 자기소개",
                example = "백엔드 개발과 해커톤에 관심이 있습니다."
        )
        String introduction,

        @Schema(
                description = "모임 태그와 정확히 일치하는 태그 개수",
                example = "2"
        )
        int exactMatchCount,

        @Schema(
                description = "후보 사용자의 취미·기술 태그 목록"
        )
        List<AiTagInfo> tags

) {

    public static AiCandidateInfo from(
            User user,
            int exactMatchCount,
            List<AiTagInfo> tags
    ) {
        return new AiCandidateInfo(
                user.getId(),
                user.getNickname(),
                user.getDepartmentId(),
                user.getPreferredRegion(),
                user.getIntroduction(),
                exactMatchCount,
                tags
        );
    }
}