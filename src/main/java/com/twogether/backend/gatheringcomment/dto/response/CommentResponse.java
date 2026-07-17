package com.twogether.backend.gatheringcomment.dto.response;

import com.twogether.backend.gatheringcomment.domain.GatheringComment;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Q&A 댓글 정보 응답")
public record CommentResponse(

        @Schema(description = "댓글 ID", example = "1")
        Long commentId,

        @Schema(description = "모임 ID", example = "1")
        Long gatheringId,

        @Schema(description = "작성자 ID", example = "3")
        Long userId,

        @Schema(description = "작성자 닉네임", example = "민우")
        String nickname,

        @Schema(description = "댓글 내용 (비밀글 권한이 없는 경우 마스킹 처리됨)", example = "이번 스터디 커리큘럼이 궁금합니다!")
        String content,

        @Schema(description = "비밀글 여부", example = "false")
        boolean isSecret,

        @Schema(description = "내가 쓴 댓글 여부", example = "false")
        boolean isMine,

        @Schema(description = "부모 댓글 ID (일반 댓글은 null)", example = "null")
        Long parentId,

        @Schema(description = "대댓글 목록")
        List<CommentResponse> children,

        @Schema(description = "등록 일시", example = "2026-07-17T13:30:00")
        LocalDateTime createdAt

) {

    public static CommentResponse of(
            GatheringComment comment,
            boolean showContent,
            boolean isMine,
            List<CommentResponse> children
    ) {
        return new CommentResponse(
                comment.getId(),
                comment.getGathering().getId(),
                comment.getUser().getId(),
                comment.getUser().getNickname(),
                showContent ? comment.getContent() : "비밀 질문입니다. 🔒",
                comment.isSecret(),
                isMine,
                comment.getParent() != null ? comment.getParent().getId() : null,
                children,
                comment.getCreatedAt()
        );
    }
}
