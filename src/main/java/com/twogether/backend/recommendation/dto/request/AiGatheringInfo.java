package com.twogether.backend.recommendation.dto.request;

import com.twogether.backend.gathering.domain.Gathering;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "AI 추천에 전달할 모임 정보")
public record AiGatheringInfo(

        @Schema(
                description = "모임 고유 ID",
                example = "1"
        )
        Long gatheringId,

        @Schema(
                description = "모임 제목",
                example = "인문·자연 연합 해커톤 팀 모집"
        )
        String title,

        @Schema(
                description = "모임 소개",
                example = "Spring Boot와 React를 활용해 서비스를 개발합니다."
        )
        String content,

        @Schema(
                description = "모임 카테고리",
                example = "HACKATHON"
        )
        String category,

        @Schema(
                description = "모임 최대 인원",
                example = "6"
        )
        int maxMembers,

        @Schema(
                description = "현재 모임 인원",
                example = "2"
        )
        int currentMembers,

        @Schema(
                description = "인문·자연 융합 모임 여부",
                example = "true"
        )
        boolean fusionEnabled,

        @Schema(
                description = "모임 태그 목록"
        )
        List<AiTagInfo> tags

) {

    public static AiGatheringInfo from(
            Gathering gathering,
            List<AiTagInfo> tags
    ) {
        return new AiGatheringInfo(
                gathering.getId(),
                gathering.getTitle(),
                gathering.getContent(),
                gathering.getCategory().name(),
                gathering.getMaxMembers(),
                gathering.getCurrentMembers(),
                gathering.isFusionEnabled(),
                tags
        );
    }
}