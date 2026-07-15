package com.twogether.backend.gatheringapplication.controller;

import com.twogether.backend.gatheringapplication.dto.request.GatheringApplicationCreateRequest;
import com.twogether.backend.gatheringapplication.dto.request.GatheringApplicationRejectRequest;
import com.twogether.backend.gatheringapplication.dto.response.GatheringApplicationAcceptResponse;
import com.twogether.backend.gatheringapplication.dto.response.GatheringApplicationCreateResponse;
import com.twogether.backend.gatheringapplication.dto.response.GatheringApplicationRejectResponse;
import com.twogether.backend.gatheringapplication.dto.response.GatheringApplicationResponse;
import com.twogether.backend.gatheringapplication.dto.response.MyApplicationResponse;
import com.twogether.backend.gatheringapplication.service.GatheringApplicationService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "모임 신청 API",
        description = "모임 참여 신청, 신청 목록 조회, 신청 수락/거절 관련 API"
)
@SecurityRequirement(name = "Bearer Authentication")
@RestController
public class GatheringApplicationController {

    private final GatheringApplicationService gatheringApplicationService;

    public GatheringApplicationController(GatheringApplicationService gatheringApplicationService) {
        this.gatheringApplicationService = gatheringApplicationService;
    }

    @Operation(
            summary = "모임 신청",
            description = """
                    유저가 모집 중인 모임에 참여를 신청합니다.

                    같은 모임에는 한 번만 신청할 수 있습니다.
                    """
    )
    @PostMapping("/api/gatherings/{gatheringId}/applications")
    public ResponseEntity<ApiResponse<GatheringApplicationCreateResponse>> applyToGathering(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long gatheringId,
            @RequestBody GatheringApplicationCreateRequest request
    ) {
        GatheringApplicationCreateResponse response =
                gatheringApplicationService.apply(jwt.getSubject(), gatheringId, request);

        return ResponseEntity.ok(
                ApiResponse.success("모임 신청이 완료되었습니다.", response)
        );
    }

    @Operation(
            summary = "특정 모임의 신청자 목록 조회",
            description = """
                    방장만 조회할 수 있습니다.

                    페이지네이션은 page=0, size=20 방식을 기본으로 합니다.
                    """
    )
    @GetMapping("/api/gatherings/{gatheringId}/applications")
    public ResponseEntity<ApiResponse<PageResponse<GatheringApplicationResponse>>> getGatheringApplications(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long gatheringId,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<GatheringApplicationResponse> response =
                gatheringApplicationService.getApplications(jwt.getSubject(), gatheringId, page, size);

        return ResponseEntity.ok(
                ApiResponse.success("신청자 목록 조회에 성공했습니다.", response)
        );
    }

    @Operation(
            summary = "내 신청 목록 조회",
            description = """
                    현재 로그인한 사용자가 신청한 모임 목록을 조회합니다.

                    페이지네이션은 page=0, size=20 방식을 기본으로 합니다.
                    """
    )
    @GetMapping("/api/users/me/applications")
    public ResponseEntity<ApiResponse<PageResponse<MyApplicationResponse>>> getMyApplications(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<MyApplicationResponse> response =
                gatheringApplicationService.getMyApplications(jwt.getSubject(), page, size);

        return ResponseEntity.ok(
                ApiResponse.success("내 신청 목록 조회에 성공했습니다.", response)
        );
    }

    @Operation(
            summary = "신청 수락",
            description = """
                    방장만 신청을 수락할 수 있습니다.

                    수락 시 gathering_application.status = ACCEPTED가 되고
                    gathering_member가 생성됩니다.
                    """
    )
    @PostMapping("/api/gathering-applications/{applicationId}/accept")
    public ResponseEntity<ApiResponse<GatheringApplicationAcceptResponse>> acceptApplication(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long applicationId
    ) {
        GatheringApplicationAcceptResponse response =
                gatheringApplicationService.accept(jwt.getSubject(), applicationId);

        return ResponseEntity.ok(
                ApiResponse.success("신청을 수락했습니다.", response)
        );
    }

    @Operation(
            summary = "신청 거절",
            description = """
                    방장만 신청을 거절할 수 있습니다.

                    거절되어도 신청 기록은 남습니다.
                    """
    )
    @PostMapping("/api/gathering-applications/{applicationId}/reject")
    public ResponseEntity<ApiResponse<GatheringApplicationRejectResponse>> rejectApplication(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long applicationId,
            @RequestBody GatheringApplicationRejectRequest request
    ) {
        GatheringApplicationRejectResponse response =
                gatheringApplicationService.reject(jwt.getSubject(), applicationId, request);

        return ResponseEntity.ok(
                ApiResponse.success("신청을 거절했습니다.", response)
        );
    }
}
