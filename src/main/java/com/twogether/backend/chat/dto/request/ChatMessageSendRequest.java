package com.twogether.backend.chat.dto.request;

import com.twogether.backend.chat.domain.MessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "메시지 전송 요청")
public record ChatMessageSendRequest(

        @Schema(description = "메시지 타입 (TEXT 또는 IMAGE)", example = "TEXT")
        @NotNull(message = "메시지 타입은 필수입니다.")
        MessageType type,

        @Schema(description = "메시지 본문", example = "안녕하세요!")
        @NotBlank(message = "메시지 본문은 비어 있을 수 없습니다.")
        String content,

        @Schema(
                description = "클라이언트 생성 멱등키(UUID). 소켓 재전송 시 중복 저장을 방지합니다.",
                example = "3f2504e0-4f89-11d3-9a0c-0305e82c3301"
        )
        UUID clientMessageId,

        @Schema(
                description = "답장 대상 메시지 ID (선택사항)",
                example = "123"
        )
        Long repliedToMessageId

) {
}
