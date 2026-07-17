package com.twogether.backend.chat.dto.response;

import com.twogether.backend.chat.domain.ChatNotice;
import com.twogether.backend.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "채팅방 공지 응답")
public record ChatNoticeResponse(

        @Schema(description = "공지 ID", example = "3")
        Long noticeId,

        @Schema(description = "채팅방 ID", example = "10")
        Long roomId,

        @Schema(description = "공지 내용", example = "정기 모임은 매주 목요일 19시입니다.")
        String content,

        @Schema(description = "고정된 메시지 ID(선택)", example = "105")
        Long messageId,

        @Schema(description = "작성자 ID", example = "1")
        Long createdByUserId,

        @Schema(description = "작성자 닉네임", example = "인준")
        String createdByNickname,

        @Schema(description = "활성 여부", example = "true")
        boolean active,

        @Schema(description = "등록 일시", example = "2026-07-14T10:00:00+09:00")
        OffsetDateTime createdAt

) {

    public static ChatNoticeResponse from(ChatNotice notice) {
        User author = notice.getCreatedBy();
        return new ChatNoticeResponse(
                notice.getId(),
                notice.getChatRoom().getId(),
                notice.getContent(),
                notice.getMessageId(),
                author == null ? null : author.getId(),
                author == null ? null : author.getNickname(),
                notice.isActive(),
                notice.getCreatedAt()
        );
    }
}
