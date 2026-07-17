package com.twogether.backend.gatheringmember.controller;

import com.twogether.backend.gatheringmember.dto.response.GatheringMemberListResponse;
import com.twogether.backend.gatheringmember.service.GatheringMemberService;
import com.twogether.backend.global.response.ApiResponse;
import com.twogether.backend.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "모임 멤버 API",
        description = "모임 멤버 목록 조회 및 탈퇴 관련 API"
)
@RestController
@RequestMapping("/api/gatherings/{gatheringId}/members")
public class GatheringMemberController {

    private final GatheringMemberService gatheringMemberService;

    public GatheringMemberController(
            GatheringMemberService gatheringMemberService
    ) {
        this.gatheringMemberService = gatheringMemberService;
    }

    @Operation(
            summary = "모임 멤버 목록 조회",
            description = """
                    모임에 참여 중인 멤버 목록을 HOST 우선·참여순으로 조회합니다.
                    페이지네이션 기본은 page=0, size=20. 없는 모임이면 404 GATHERING_NOT_FOUND.
                    """
    )
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<GatheringMemberListResponse>>> getGatheringMembers(
            @PathVariable Long gatheringId,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<GatheringMemberListResponse> response =
                gatheringMemberService.getMembers(gatheringId, page, size);

        return ResponseEntity.ok(
                ApiResponse.success("모임 멤버 목록 조회에 성공했습니다.", response)
        );
    }

    @Operation(
            summary = "모임 나가기",
            description = """
                    일반 멤버만 나갈 수 있습니다.

                    방장은 바로 나갈 수 없고, 모임 취소 또는 방장 위임 정책이 필요합니다.
                    """
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> leaveGathering(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long gatheringId
    ) {
        gatheringMemberService.leaveMember(jwt.getSubject(), gatheringId);

        return ResponseEntity.ok(
                ApiResponse.success("모임에서 나갔습니다.")
        );
    }

    @Operation(
            summary = "멤버 추방",
            description = "방장만 특정 멤버를 모임에서 추방할 수 있습니다. 추방 시 멤버와 연결된 수락 신청도 함께 삭제됩니다."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> expelMember(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long gatheringId,
            @PathVariable Long userId
    ) {
        gatheringMemberService.expelMember(jwt.getSubject(), gatheringId, userId);

        return ResponseEntity.ok(
                ApiResponse.success("멤버를 추방했습니다.")
        );
    }
}
