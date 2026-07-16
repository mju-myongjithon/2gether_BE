package com.twogether.backend.gathering.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 모임 부분 수정(PATCH) 요청.
 *
 * 모든 필드는 선택값이다.
 * - 일반 필드: null 이면 기존 값 유지, 값이 있으면 교체.
 * - tags/images: null 이면 유지, 목록을 보내면 통째로 교체(빈 목록이면 전체 삭제).
 * 검증(@Size/@Min)은 값이 존재할 때만 적용된다.
 */
@Schema(description = "모임 수정 요청(부분 수정, 보낸 필드만 반영)")
public record GatheringUpdateRequest(

        @Schema(description = "모임 제목(미전송 시 유지)", example = "해커톤 팀원 모집")
        @Size(max = 120, message = "모임 제목은 120자 이하로 입력해주세요.")
        String title,

        @Schema(description = "모임 소개(미전송 시 유지)", example = "기획, 디자인, 개발 같이 할 사람 구합니다.")
        @Size(max = 5000, message = "모임 소개는 5000자 이하로 입력해주세요.")
        String content,

        @Schema(description = "모임 카테고리 enum 값(미전송 시 유지)", example = "HACKATHON")
        String category,

        @Schema(description = "모임 장소(미전송 시 유지)", example = "인문캠 학생회관")
        @Size(max = 150, message = "모임 장소는 150자 이하로 입력해주세요.")
        String location,

        @Schema(description = "최대 인원(미전송 시 유지, 현재 참여 인원 이상)", example = "6")
        @Min(value = 1, message = "최대 인원은 1명 이상이어야 합니다.")
        Integer maxMembers,

        @Schema(description = "인문X자연 융합 모임 여부(미전송 시 유지)", example = "true")
        Boolean fusionEnabled,

        @Schema(description = "모임 예정 일시 ISO 8601(미전송 시 유지)", example = "2026-07-16T18:00:00+09:00")
        OffsetDateTime meetAt,

        @Schema(description = "모임 태그 ID 목록(미전송 시 유지, 목록 전송 시 통째 교체, 빈 목록이면 전체 삭제)", example = "[1, 2]")
        List<Long> tagIds,

        @Schema(description = "모임 이미지 URL 목록(미전송 시 유지, 목록 전송 시 통째 교체, 최대 5장)", example = "[\"https://cdn.example.com/a.png\"]")
        @Size(max = 5, message = "이미지는 최대 5장까지 등록할 수 있습니다.")
        List<@Size(max = 300, message = "이미지 URL은 300자 이하여야 합니다.") String> imageUrls

) {
}
