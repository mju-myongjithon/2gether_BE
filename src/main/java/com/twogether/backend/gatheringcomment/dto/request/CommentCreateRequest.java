package com.twogether.backend.gatheringcomment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "댓글 생성 요청")
public record CommentCreateRequest(

        @Schema(description = "댓글 내용", example = "이번 스터디 커리큘럼이 궁금합니다!")
        @NotBlank(message = "댓글 내용은 비어 있을 수 없습니다.")
        @Size(max = 1000, message = "댓글은 1000자 이하로 입력해주세요.")
        String content,

        @Schema(description = "비밀글 여부(방장과 작성자만 보기 가능)", example = "false")
        boolean isSecret,

        @Schema(description = "부모 댓글 ID (대댓글인 경우에만 입력, 일반 댓글은 null)", example = "null")
        Long parentId

) {
}
