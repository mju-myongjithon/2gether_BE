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
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
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

    public GatheringApplicationController(
            GatheringApplicationService gatheringApplicationService
    ) {
        this.gatheringApplicationService = gatheringApplicationService;
    }

    @Operation(
            summary = "모임 신청",
            description = """
                    유저가 모집 중(RECRUITING)인 모임에 참여를 신청합니다.

                    - 이미 참여 중(방장 포함)이면 409 ALREADY_MEMBER.
                    - 활성 신청(대기/수락)이 있으면 409 DUPLICATE_APPLICATION. 거절(REJECTED) 후에는 재신청 가능.
                    - 모집중이 아니면 409 GATHERING_NOT_RECRUITING, 없는 모임이면 404 GATHERING_NOT_FOUND.
                    """
    )
    @PostMapping("/api/gatherings/{gatheringId}/applications")
    public ResponseEntity<ApiResponse<GatheringApplicationCreateResponse>> applyToGathering(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long gatheringId,
            @Valid @RequestBody GatheringApplicationCreateRequest request
    ) {
        GatheringApplicationCreateResponse response =
                gatheringApplicationService.apply(jwt.getSubject(), gatheringId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("모임 신청이 완료되었습니다.", response));
    }

    @Operation(
            summary = "특정 모임의 신청자 목록 조회",
            description = """
                    방장만 조회할 수 있습니다. status 로 신청 상태(PENDING/ACCEPTED/REJECTED)를 필터링할 수 있고,
                    미지정 시 전체를 신청 최신순으로 반환합니다. 페이지네이션 기본은 page=0, size=20.

                    방장이 아니면 403 FORBIDDEN, 없는 모임이면 404 GATHERING_NOT_FOUND.
                    """
    )
    @GetMapping("/api/gatherings/{gatheringId}/applications")
    public ResponseEntity<ApiResponse<PageResponse<GatheringApplicationResponse>>> getGatheringApplications(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long gatheringId,
            @Parameter(description = "신청 상태 필터 enum 값", example = "PENDING") @RequestParam(required = false) String status,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<GatheringApplicationResponse> pageResponse =
                gatheringApplicationService.getApplications(jwt.getSubject(), gatheringId, status, page, size);

        return ResponseEntity.ok(
                ApiResponse.success("신청자 목록 조회에 성공했습니다.", pageResponse)
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
        PageResponse<MyApplicationResponse> pageResponse =
                gatheringApplicationService.getMyApplications(jwt.getSubject(), page, size);

        return ResponseEntity.ok(
                ApiResponse.success("내 신청 목록 조회에 성공했습니다.", pageResponse)
        );
    }

    @Operation(
            summary = "내 신청 취소",
            description = "본인이 신청한 PENDING 신청만 취소할 수 있으며, 취소 시 신청 레코드를 삭제합니다."
    )
    @DeleteMapping("/api/gathering-applications/{applicationId}")
    public ResponseEntity<ApiResponse<Void>> cancelMyApplication(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long applicationId
    ) {
        gatheringApplicationService.cancelMyApplication(jwt.getSubject(), applicationId);

        return ResponseEntity.ok(
                ApiResponse.success("신청이 취소되었습니다.")
        );
    }

    @Operation(
            summary = "신청 수락",
            description = """
                    방장만 신청을 수락할 수 있습니다.

                    수락 시 status = ACCEPTED 로 바뀌고 gathering_member 가 생성되며 current_members 가 증가합니다(한 트랜잭션).

                    방장 아님 403 FORBIDDEN, 이미 처리된 신청 409 APPLICATION_ALREADY_PROCESSED,
                    정원 초과 409 CAPACITY_EXCEEDED, 없는 신청 404 APPLICATION_NOT_FOUND.
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
                    방장만 신청을 거절할 수 있습니다. 거절 사유를 저장하며 신청 기록은 남습니다(status = REJECTED).

                    방장 아님 403 FORBIDDEN, 이미 처리된 신청 409 APPLICATION_ALREADY_PROCESSED,
                    없는 신청 404 APPLICATION_NOT_FOUND.
                    """
    )
    @PostMapping("/api/gathering-applications/{applicationId}/reject")
    public ResponseEntity<ApiResponse<GatheringApplicationRejectResponse>> rejectApplication(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long applicationId,
            @Valid @RequestBody GatheringApplicationRejectRequest request
    ) {
        GatheringApplicationRejectResponse response =
                gatheringApplicationService.reject(jwt.getSubject(), applicationId, request);

        return ResponseEntity.ok(
                ApiResponse.success("신청을 거절했습니다.", response)
        );
    }
}
