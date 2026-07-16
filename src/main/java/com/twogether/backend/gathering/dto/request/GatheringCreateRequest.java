package com.twogether.backend.gathering.dto.request;

import com.twogether.backend.gathering.domain.GatheringCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "모임 생성 요청")
public record GatheringCreateRequest(

        @Schema(description = "모임 제목", example = "인문X자연 해커톤 팀 모집")
        @NotBlank(message = "모임 제목은 필수입니다.")
        @Size(max = 120, message = "모임 제목은 120자 이하로 입력해주세요.")
        String title,

        @Schema(description = "모임 소개", example = "기획, 디자인, 개발 같이 할 사람 구합니다.")
        @Size(max = 5000, message = "모임 소개는 5000자 이하로 입력해주세요.")
        String content,

        @Schema(description = "모임 카테고리", example = "HACKATHON")
        @NotNull(message = "모임 카테고리는 필수입니다.")
        GatheringCategory category,

        @Schema(description = "모임 장소", example = "자연캠 명진당")
        @Size(max = 150, message = "모임 장소는 150자 이하로 입력해주세요.")
        String location,

        @Schema(description = "최대 인원", example = "6")
        @Min(value = 1, message = "최대 인원은 1명 이상이어야 합니다.")
        int maxMembers,

        @Schema(description = "인문X자연 융합 모임 여부", example = "true")
        boolean fusionEnabled,

        @Schema(description = "모집 시작 일시 (ISO 8601, null이면 생성 즉시 모집)", example = "2026-07-10T09:00:00+09:00")
        OffsetDateTime recruitStartAt,

        @Schema(description = "모집 종료 일시 (ISO 8601, null이면 상시 모집)", example = "2026-07-14T23:59:00+09:00")
        OffsetDateTime recruitEndAt,

        @Schema(description = "모임 예정 일시 (ISO 8601)", example = "2026-07-15T18:00:00+09:00")
        OffsetDateTime meetAt,

        @Schema(description = "모임 태그 ID 목록", example = "[1, 2]")
        List<Long> tagIds,

        @Schema(description = "모임 이미지 URL 목록 (최대 5장)", example = "[\"https://cdn.example.com/a.png\"]")
        @Size(max = 5, message = "이미지는 최대 5장까지 등록할 수 있습니다.")
        List<@Size(max = 300, message = "이미지 URL은 300자 이하여야 합니다.") String> imageUrls

) {
}
