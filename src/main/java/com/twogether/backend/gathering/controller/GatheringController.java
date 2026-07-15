package com.twogether.backend.gathering.controller;

import com.twogether.backend.gathering.domain.GatheringStatus;
import com.twogether.backend.gathering.dto.request.GatheringCreateRequest;
import com.twogether.backend.gathering.dto.request.GatheringUpdateRequest;
import com.twogether.backend.gathering.dto.response.GatheringCancelResponse;
import com.twogether.backend.gathering.dto.response.GatheringConfirmResponse;
import com.twogether.backend.gathering.dto.response.GatheringCreateResponse;
import com.twogether.backend.gathering.dto.response.GatheringDetailResponse;
import com.twogether.backend.gathering.dto.response.GatheringSummaryResponse;
import com.twogether.backend.gathering.dto.response.GatheringUpdateResponse;
import com.twogether.backend.gathering.service.GatheringService;
import com.twogether.backend.global.response.ApiResponse;
import com.twogether.backend.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "모임 API",
        description = "모임 생성, 조회, 수정, 취소, 확정 관련 API"
)
@RestController
@RequestMapping("/api/gatherings")
public class GatheringController {

    private final GatheringService gatheringService;

    public GatheringController(GatheringService gatheringService) {
        this.gatheringService = gatheringService;
    }

    @Operation(
            summary = "모임 생성",
            description = """
                    방장이 직접 모임을 생성합니다.

                    생성자는 자동으로 gathering_member에 HOST로 등록됩니다.

                    Supabase 로그인 후 발급받은 access token을
                    Authorization 헤더에 Bearer 형식으로 전달해야 합니다.
                    """
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping
    public ResponseEntity<ApiResponse<GatheringCreateResponse>> createGathering(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody GatheringCreateRequest request
    ) {
        GatheringCreateResponse response =
                gatheringService.create(jwt.getSubject(), request);

        return ResponseEntity.ok(
                ApiResponse.success("모임이 생성되었습니다.", response)
        );
    }

    @Operation(
            summary = "모임 목록 조회",
            description = """
                    카테고리, 상태, 검색어 기준으로 모임 목록을 조회합니다.

                    로그인 없이 조회할 수 있습니다.

                    페이지네이션은 page=0, size=20 방식을 기본으로 합니다.
                    """
    )
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<GatheringSummaryResponse>>> getGatherings(
            @Parameter(description = "모임 카테고리") @RequestParam(required = false) String category,
            @Parameter(description = "모임 상태") @RequestParam(required = false) GatheringStatus status,
            @Parameter(description = "제목/내용 검색어") @RequestParam(required = false) String keyword,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<GatheringSummaryResponse> response =
                gatheringService.list(category, status, keyword, page, size);

        return ResponseEntity.ok(
                ApiResponse.success("모임 목록 조회에 성공했습니다.", response)
        );
    }

    @Operation(
            summary = "모임 상세 조회",
            description = """
                    모임의 상세 정보, 멤버 목록, 캠퍼스 비율, 내 신청/참여 상태를 조회합니다.

                    로그인 없이 조회할 수 있습니다. 다만 로그인한 경우에만
                    myApplicationStatus/isHost/isMember가 실제 값으로 채워지고,
                    비로그인이면 각각 null/false/false로 내려갑니다.
                    """
    )
    @GetMapping("/{gatheringId}")
    public ResponseEntity<ApiResponse<GatheringDetailResponse>> getGathering(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long gatheringId
    ) {
        String authUserId = jwt == null ? null : jwt.getSubject();

        GatheringDetailResponse response =
                gatheringService.getDetail(gatheringId, authUserId);

        return ResponseEntity.ok(
                ApiResponse.success("모임 상세 조회에 성공했습니다.", response)
        );
    }

    @Operation(
            summary = "모임 수정",
            description = """
                    모집 중인 모임만 수정 가능합니다.

                    방장만 수정할 수 있습니다.

                    imageUrls는 전달된 목록으로 기존 이미지를 전체 교체합니다.
                    """
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PatchMapping("/{gatheringId}")
    public ResponseEntity<ApiResponse<GatheringUpdateResponse>> updateGathering(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long gatheringId,
            @RequestBody GatheringUpdateRequest request
    ) {
        GatheringUpdateResponse response =
                gatheringService.update(jwt.getSubject(), gatheringId, request);

        return ResponseEntity.ok(
                ApiResponse.success("모임이 수정되었습니다.", response)
        );
    }

    @Operation(
            summary = "모임 취소",
            description = """
                    모집 중인 모임만 취소 가능합니다.

                    물리 삭제가 아니라 status = CANCELED로 변경합니다.

                    방장만 취소할 수 있습니다.
                    """
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/{gatheringId}/cancel")
    public ResponseEntity<ApiResponse<GatheringCancelResponse>> cancelGathering(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long gatheringId
    ) {
        GatheringCancelResponse response =
                gatheringService.cancel(jwt.getSubject(), gatheringId);

        return ResponseEntity.ok(
                ApiResponse.success("모임이 취소되었습니다.", response)
        );
    }

    @Operation(
            summary = "모임 확정",
            description = """
                    방장이 모집을 마감합니다.

                    확정 시 status = CONFIRMED로 변경됩니다.

                    채팅 도메인이 아직 실제 DB와 연동되지 않아
                    chatRoomId는 당분간 null로 내려갑니다.
                    """
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/{gatheringId}/confirm")
    public ResponseEntity<ApiResponse<GatheringConfirmResponse>> confirmGathering(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long gatheringId
    ) {
        GatheringConfirmResponse response =
                gatheringService.confirm(jwt.getSubject(), gatheringId);

        return ResponseEntity.ok(
                ApiResponse.success("모임이 확정되었습니다.", response)
        );
    }
}
