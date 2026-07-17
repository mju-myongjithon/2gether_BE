package com.twogether.backend.gatheringinvitation.controller;

import com.twogether.backend.gatheringinvitation.dto.request.GatheringInvitationCreateRequest;
import com.twogether.backend.gatheringinvitation.dto.response.GatheringInvitationResponse;
import com.twogether.backend.gatheringinvitation.service.GatheringInvitationService;
import com.twogether.backend.global.response.ApiResponse;
import com.twogether.backend.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "모임 초대 API",
        description = "모임 초대 발송, 초대 목록 조회, 초대 수락/거절/취소 관련 API"
)
@SecurityRequirement(name = "Bearer Authentication")
@RestController
public class GatheringInvitationController {

    private final GatheringInvitationService invitationService;

    public GatheringInvitationController(GatheringInvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @Operation(
            summary = "모임 초대 발송",
            description = """
                    방장이 특정 사용자를 모임에 초대합니다.

                    - 방장만 초대 가능
                    - 이미 멤버인 경우 409 ALREADY_MEMBER
                    - 활성 초대가 있으면 409 DUPLICATE_APPLICATION
                    """
    )
    @PostMapping("/api/gatherings/{gatheringId}/invitations")
    public ResponseEntity<ApiResponse<GatheringInvitationResponse>> inviteToGathering(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long gatheringId,
            @Valid @RequestBody GatheringInvitationCreateRequest request
    ) {
        GatheringInvitationResponse response = invitationService.invite(
                jwt.getSubject(),
                gatheringId,
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("초대가 발송되었습니다.", response));
    }

    @Operation(
            summary = "방장이 보낸 초대 목록 조회",
            description = """
                    방장만 조회할 수 있습니다. status로 초대 상태(PENDING/ACCEPTED/REJECTED/CANCELLED)를 필터링할 수 있고,
                    미지정 시 전체를 초대 최신순으로 반환합니다.
                    """
    )
    @GetMapping("/api/gatherings/{gatheringId}/invitations")
    public ResponseEntity<ApiResponse<PageResponse<GatheringInvitationResponse>>> getSentInvitations(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long gatheringId,
            @Parameter(description = "초대 상태 필터", example = "PENDING") @RequestParam(required = false) String status,
            @PageableDefault(size = 20, sort = "invitedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<GatheringInvitationResponse> page = invitationService.getSentInvitations(
                jwt.getSubject(),
                gatheringId,
                status,
                pageable
        );

        return ResponseEntity.ok(
                ApiResponse.success("보낸 초대 목록 조회에 성공했습니다.",
                        PageResponse.of(
                                page.getContent(),
                                page.getNumber(),
                                page.getSize(),
                                page.getTotalElements()
                        ))
        );
    }

    @Operation(
            summary = "사용자가 받은 초대 목록 조회",
            description = """
                    현재 로그인한 사용자가 받은 초대 목록을 조회합니다.
                    status로 초대 상태를 필터링할 수 있습니다.
                    """
    )
    @GetMapping("/api/users/me/invitations")
    public ResponseEntity<ApiResponse<PageResponse<GatheringInvitationResponse>>> getReceivedInvitations(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "초대 상태 필터", example = "PENDING") @RequestParam(required = false) String status,
            @PageableDefault(size = 20, sort = "invitedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<GatheringInvitationResponse> page = invitationService.getReceivedInvitations(
                jwt.getSubject(),
                status,
                pageable
        );

        return ResponseEntity.ok(
                ApiResponse.success("받은 초대 목록 조회에 성공했습니다.",
                        PageResponse.of(
                                page.getContent(),
                                page.getNumber(),
                                page.getSize(),
                                page.getTotalElements()
                        ))
        );
    }

    @Operation(
            summary = "초대 수락",
            description = """
                    초대받은 사용자가 초대를 수락하고 모임에 참여합니다.

                    수락 시 초대 상태가 ACCEPTED로 변경됩니다.
                    """
    )
    @PostMapping("/api/invitations/{invitationId}/accept")
    public ResponseEntity<ApiResponse<GatheringInvitationResponse>> acceptInvitation(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long invitationId
    ) {
        GatheringInvitationResponse response = invitationService.accept(
                jwt.getSubject(),
                invitationId
        );

        return ResponseEntity.ok(
                ApiResponse.success("초대를 수락했습니다.", response)
        );
    }

    @Operation(
            summary = "초대 거절",
            description = """
                    초대받은 사용자가 초대를 거절합니다.
                    """
    )
    @PostMapping("/api/invitations/{invitationId}/reject")
    public ResponseEntity<ApiResponse<GatheringInvitationResponse>> rejectInvitation(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long invitationId,
            @Parameter(description = "거절 사유") @RequestParam(required = false) String reason
    ) {
        GatheringInvitationResponse response = invitationService.reject(
                jwt.getSubject(),
                invitationId,
                reason
        );

        return ResponseEntity.ok(
                ApiResponse.success("초대를 거절했습니다.", response)
        );
    }

    @Operation(
            summary = "초대 취소",
            description = """
                    방장이 보낸 PENDING 상태의 초대만 취소할 수 있습니다.
                    """
    )
    @DeleteMapping("/api/invitations/{invitationId}")
    public ResponseEntity<ApiResponse<Void>> cancelInvitation(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long invitationId
    ) {
        invitationService.cancel(jwt.getSubject(), invitationId);

        return ResponseEntity.ok(
                ApiResponse.success("초대가 취소되었습니다.")
        );
    }
}
