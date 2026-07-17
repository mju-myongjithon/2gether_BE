package com.twogether.backend.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "이미지 첨부 항목 (클라이언트가 스토리지 업로드 후 확보한 URL)")
public record ImageAttachmentRequest(

        @Schema(description = "원본 파일 URL", example = "https://cdn.example.com/chat/abc.jpg")
        @NotBlank(message = "파일 URL은 필수입니다.")
        @Size(max = 300, message = "파일 URL은 300자 이하여야 합니다.")
        String fileUrl,

        @Schema(description = "썸네일 URL", example = "https://cdn.example.com/chat/abc_thumb.jpg")
        @Size(max = 300, message = "썸네일 URL은 300자 이하여야 합니다.")
        String thumbnailUrl,

        @Schema(description = "MIME 타입", example = "image/jpeg")
        @Size(max = 100, message = "콘텐츠 타입은 100자 이하여야 합니다.")
        String contentType,

        @Schema(description = "파일 크기(byte)", example = "204800")
        Integer size,

        @Schema(description = "가로 픽셀", example = "1080")
        Integer width,

        @Schema(description = "세로 픽셀", example = "1350")
        Integer height

) {
}
