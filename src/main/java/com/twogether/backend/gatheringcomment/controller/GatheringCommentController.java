package com.twogether.backend.gatheringcomment.controller;

import com.twogether.backend.gatheringcomment.dto.request.CommentCreateRequest;
import com.twogether.backend.gatheringcomment.dto.response.CommentResponse;
import com.twogether.backend.gatheringcomment.service.GatheringCommentService;
import com.twogether.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(
        name = "모임 Q&A 댓글 API",
        description = "모임 내 댓글 및 질문답변 등록, 조회, 삭제 관련 API"
)
@RestController
@RequestMapping("/api/gatherings/{gatheringId}/comments")
public class GatheringCommentController {

    private final GatheringCommentService gatheringCommentService;

    public GatheringCommentController(
            GatheringCommentService gatheringCommentService
    ) {
        this.gatheringCommentService = gatheringCommentService;
    }

    @Operation(
            summary = "댓글/질문 등록",
            description = """
                    모임에 댓글 또는 질문을 남깁니다.
                    
                    - 비로그인 유저는 작성할 수 없습니다.
                    - parentId 를 제공하면 대댓글(답글)로 생성되며, 부모 댓글이 비밀글인 경우 자동으로 비밀 답글이 됩니다.
                    """
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long gatheringId,
            @Valid @RequestBody CommentCreateRequest request
    ) {
        String authUserId = jwt.getSubject();
        CommentResponse response = gatheringCommentService.create(authUserId, gatheringId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("댓글이 등록되었습니다.", response));
    }

    @Operation(
            summary = "댓글 삭제",
            description = "본인이 작성한 댓글이거나, 해당 모임의 방장인 경우에만 삭제할 수 있습니다."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long gatheringId,
            @PathVariable Long commentId
    ) {
        String authUserId = jwt.getSubject();
        gatheringCommentService.delete(authUserId, gatheringId, commentId);
        return ResponseEntity.ok(ApiResponse.success("댓글이 삭제되었습니다."));
    }

    @Operation(
            summary = "댓글/Q&A 목록 조회",
            description = """
                    모임의 전체 댓글 트리를 계층형(children) 구조로 조회합니다.
                    
                    - 비로그인 조회를 허용하며, 로그인 유저가 방장이나 댓글/부모글 작성자가 아닌 경우 비밀댓글(isSecret=true)의 내용은 마스킹되어 내려갑니다.
                    """
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long gatheringId
    ) {
        String authUserId = (jwt != null) ? jwt.getSubject() : null;
        List<CommentResponse> response = gatheringCommentService.getComments(authUserId, gatheringId);
        return ResponseEntity.ok(ApiResponse.success("Q&A 댓글 목록 조회에 성공했습니다.", response));
    }
}
