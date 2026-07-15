package com.twogether.backend.gathering.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "모임 생성 요청")
public record GatheringCreateRequest(

        @Schema(description = "모임 제목", example = "인문X자연 해커톤 팀 모집")
        String title,

        @Schema(description = "모임 소개", example = "기획, 디자인, 개발 같이 할 사람 구합니다.")
        String content,

        @Schema(description = "모임 카테고리", example = "해커톤")
        String category,

        @Schema(description = "모임 장소", example = "자연캠 명진당")
        String location,

        @Schema(description = "최대 인원", example = "6")
        int maxMembers,

        @Schema(description = "인문X자연 융합 모임 여부", example = "true")
        boolean fusionEnabled,

        @Schema(
                description = "모집 시작 일시 (ISO 8601). 비워두면 즉시 모집이 시작된 것으로 간주합니다.",
                example = "2026-07-10T00:00:00+09:00",
                nullable = true
        )
        OffsetDateTime recruitStartAt,

        @Schema(
                description = "모집 마감 일시 (ISO 8601). 비워두면 마감일 없이 상시 모집으로 처리됩니다.",
                example = "2026-07-14T23:59:59+09:00",
                nullable = true
        )
        OffsetDateTime recruitEndAt,

        @Schema(description = "모임 예정 일시 (ISO 8601)", example = "2026-07-15T18:00:00+09:00")
        OffsetDateTime meetAt,

        @Schema(
                description = "모임 이미지 URL 목록 (최대 5개, 등록한 순서대로 노출)",
                example = "[\"https://cdn.2gether.app/gatherings/1/1.png\"]",
                nullable = true
        )
        List<String> imageUrls

) {
}
