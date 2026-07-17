package com.twogether.backend.chat.dto.response;

import com.twogether.backend.chat.domain.MessageAttachment;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "메시지 첨부(이미지/파일) 응답")
public record MessageAttachmentResponse(

        @Schema(description = "첨부 ID", example = "1")
        Long attachmentId,

        @Schema(description = "원본 파일 URL", example = "https://cdn.example.com/chat/abc.jpg")
        String fileUrl,

        @Schema(description = "썸네일 URL", example = "https://cdn.example.com/chat/abc_thumb.jpg")
        String thumbnailUrl,

        @Schema(description = "MIME 타입", example = "image/jpeg")
        String contentType,

        @Schema(description = "파일 크기(byte)", example = "204800")
        Integer size,

        @Schema(description = "가로 픽셀", example = "1080")
        Integer width,

        @Schema(description = "세로 픽셀", example = "1350")
        Integer height,

        @Schema(description = "정렬 순서", example = "0")
        int sortOrder

) {

    public static MessageAttachmentResponse from(MessageAttachment attachment) {
        return new MessageAttachmentResponse(
                attachment.getId(),
                attachment.getFileUrl(),
                attachment.getThumbnailUrl(),
                attachment.getContentType(),
                attachment.getSize(),
                attachment.getWidth(),
                attachment.getHeight(),
                attachment.getSortOrder()
        );
    }
}
