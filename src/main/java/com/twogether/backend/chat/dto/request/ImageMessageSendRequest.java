package com.twogether.backend.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

@Schema(description = "이미지 메시지 전송 요청 (첨부는 message_attachment로 저장)")
public record ImageMessageSendRequest(

        @Schema(description = "이미지에 곁들일 캡션(선택)", example = "여기 어때요?")
        String caption,

        @Schema(
                description = "클라이언트 생성 멱등키(UUID). 소켓/재시도 시 중복 저장 방지",
                example = "3f2504e0-4f89-11d3-9a0c-0305e82c3301"
        )
        UUID clientMessageId,

        @Schema(description = "첨부 이미지 목록(최소 1개)")
        @NotEmpty(message = "이미지 첨부는 최소 1개 이상이어야 합니다.")
        @Valid
        List<ImageAttachmentRequest> attachments

) {
}
