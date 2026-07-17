package com.twogether.backend.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "채팅방 공지 등록 요청")
public record ChatNoticeCreateRequest(

        @Schema(description = "공지 내용", example = "정기 모임은 매주 목요일 19시입니다.")
        @NotBlank(message = "공지 내용은 비어 있을 수 없습니다.")
        @Size(max = 500, message = "공지 내용은 500자 이하여야 합니다.")
        String content,

        @Schema(description = "공지로 고정할 메시지 ID(선택)", example = "105")
        Long messageId

) {
}
