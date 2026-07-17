package com.twogether.backend.gatheringnotice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "공지사항 생성/수정 요청")
public record NoticeCreateRequest(

        @Schema(description = "공지 제목", example = "중요 공지사항입니다.")
        @NotBlank(message = "제목은 비어 있을 수 없습니다.")
        @Size(max = 100, message = "제목은 100자 이하로 작성해주세요.")
        String title,

        @Schema(description = "공지 내용", example = "이번 주 모임 장소가 명진당 1층으로 변경되었습니다.")
        @NotBlank(message = "내용은 비어 있을 수 없습니다.")
        @Size(max = 2000, message = "내용은 2000자 이하로 작성해주세요.")
        String content,

        @Schema(description = "상단 고정 여부", example = "true")
        boolean isPinned

) {
}
