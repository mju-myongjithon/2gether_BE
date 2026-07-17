package com.twogether.backend.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "메시지 커서 페이지 응답 (최신 → 과거 순)")
public record ChatMessagePageResponse(

        @Schema(description = "메시지 목록 (id 내림차순: 최신 → 과거)")
        List<ChatMessageResponse> content,

        @Schema(description = "다음 페이지 조회에 사용할 커서(마지막 항목의 messageId). 더 없으면 null", example = "100")
        Long nextCursor,

        @Schema(description = "다음(더 과거) 페이지 존재 여부", example = "true")
        boolean hasNext,

        @Schema(description = "페이지 크기", example = "20")
        int size

) {

    public static ChatMessagePageResponse of(
            List<ChatMessageResponse> content,
            Long nextCursor,
            boolean hasNext,
            int size
    ) {
        return new ChatMessagePageResponse(content, nextCursor, hasNext, size);
    }
}
