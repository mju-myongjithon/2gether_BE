package com.twogether.backend.bookmark.dto.response;

import com.twogether.backend.tag.dto.response.HobbyTagResponse;
import com.twogether.backend.tag.dto.response.SkillTagResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "북마크한 사용자 응답")
public record BookmarkedUserResponse(

        @Schema(description = "사용자 고유 ID", example = "2")
        Long userId,

        @Schema(description = "닉네임", example = "인준")
        String nickname,

        @Schema(description = "사용자 나이", example = "24")
        Integer age,

        @Schema(description = "학과명", example = "컴퓨터공학과")
        String departmentName,

        @Schema(description = "캠퍼스", example = "NATURAL")
        String campus,

        @Schema(description = "선호 활동 지역", example = "서울")
        String preferredRegion,

        @Schema(description = "프로필 이미지 URL")
        String profileImageUrl,

        @Schema(description = "자기소개")
        String introduction,

        @Schema(description = "관심사 태그 목록")
        List<HobbyTagResponse> hobbyTags,

        @Schema(description = "스킬 태그 목록")
        List<SkillTagResponse> skillTags
) {
}