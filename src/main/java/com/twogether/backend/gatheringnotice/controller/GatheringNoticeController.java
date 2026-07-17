package com.twogether.backend.gatheringnotice.controller;

import com.twogether.backend.gatheringnotice.dto.request.NoticeCreateRequest;
import com.twogether.backend.gatheringnotice.dto.response.NoticeResponse;
import com.twogether.backend.gatheringnotice.service.GatheringNoticeService;
import com.twogether.backend.global.response.ApiResponse;
import com.twogether.backend.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "모임 공지사항 API",
        description = "모임 내 공지사항 등록, 조회, 수정, 삭제 관련 API"
)
@RestController
@RequestMapping("/api/gatherings/{gatheringId}/notices")
public class GatheringNoticeController {

    private final GatheringNoticeService gatheringNoticeService;

    public GatheringNoticeController(
            GatheringNoticeService gatheringNoticeService
    ) {
        this.gatheringNoticeService = gatheringNoticeService;
    }

    @Operation(
            summary = "공지사항 등록",
            description = "방장만 해당 모임에 공지사항을 생성할 수 있습니다."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping
    public ResponseEntity<ApiResponse<NoticeResponse>> createNotice(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long gatheringId,
            @Valid @RequestBody NoticeCreateRequest request
    ) {
        String authUserId = jwt.getSubject();
        NoticeResponse response = gatheringNoticeService.create(authUserId, gatheringId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("공지사항이 등록되었습니다.", response));
    }

    @Operation(
            summary = "공지사항 수정",
            description = "방장만 해당 공지사항을 수정할 수 있습니다."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PatchMapping("/{noticeId}")
    public ResponseEntity<ApiResponse<NoticeResponse>> updateNotice(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long gatheringId,
            @PathVariable Long noticeId,
            @Valid @RequestBody NoticeCreateRequest request
    ) {
        String authUserId = jwt.getSubject();
        NoticeResponse response = gatheringNoticeService.update(authUserId, gatheringId, noticeId, request);
        return ResponseEntity.ok(ApiResponse.success("공지사항이 수정되었습니다.", response));
    }

    @Operation(
            summary = "공지사항 삭제",
            description = "방장만 해당 공지사항을 삭제할 수 있습니다."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/{noticeId}")
    public ResponseEntity<ApiResponse<Void>> deleteNotice(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long gatheringId,
            @PathVariable Long noticeId
    ) {
        String authUserId = jwt.getSubject();
        gatheringNoticeService.delete(authUserId, gatheringId, noticeId);
        return ResponseEntity.ok(ApiResponse.success("공지사항이 삭제되었습니다."));
    }

    @Operation(
            summary = "공지사항 목록 조회",
            description = "해당 모임의 참여 확정 멤버 및 방장만 공지 목록을 조회할 수 있습니다."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NoticeResponse>>> getNotices(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long gatheringId,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size
    ) {
        String authUserId = jwt.getSubject();
        PageResponse<NoticeResponse> response = gatheringNoticeService.getNotices(authUserId, gatheringId, page, size);
        return ResponseEntity.ok(ApiResponse.success("공지사항 목록 조회에 성공했습니다.", response));
    }
}
