package com.twogether.backend.chat.dto.response;

import com.twogether.backend.chat.domain.Message;
import com.twogether.backend.chat.domain.MessageType;
import com.twogether.backend.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "채팅 메시지 응답")
public record ChatMessageResponse(

        @Schema(description = "메시지 ID", example = "105")
        Long messageId,

        @Schema(description = "채팅방 ID", example = "10")
        Long roomId,

        @Schema(description = "발신자 ID (SYSTEM 메시지는 null)", example = "1")
        Long senderId,

        @Schema(description = "발신자 닉네임 (SYSTEM 메시지는 null)", example = "인준")
        String senderNickname,

        @Schema(description = "메시지 타입", example = "TEXT")
        MessageType type,

        @Schema(description = "메시지 본문 (IMAGE는 캡션, 없으면 null)", example = "내일 7시에 봬요!")
        String content,

        @Schema(description = "첨부 목록 (IMAGE 메시지에서 사용, 그 외 빈 목록)")
        List<MessageAttachmentResponse> attachments,

        @Schema(description = "전송 일시", example = "2026-07-13T21:10:00+09:00")
        OffsetDateTime createdAt

) {

    public static ChatMessageResponse from(Message message) {
        return from(message, List.of());
    }

    public static ChatMessageResponse from(
            Message message,
            List<MessageAttachmentResponse> attachments
    ) {
        User sender = message.getSender();
        return new ChatMessageResponse(
                message.getId(),
                message.getChatRoom().getId(),
                sender == null ? null : sender.getId(),
                sender == null ? null : sender.getNickname(),
                message.getType(),
                message.getContent(),
                attachments,
                message.getCreatedAt()
        );
    }
}
