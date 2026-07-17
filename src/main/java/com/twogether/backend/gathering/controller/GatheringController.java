package com.twogether.backend.gathering.controller;

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
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

import java.util.List;

@Tag(
        name = "모임 API",
        description = "모임 생성, 조회, 수정, 취소, 확정 관련 API"
)
@RestController
@RequestMapping("/api/gatherings")
public class GatheringController {

    private final GatheringService gatheringService;

    public GatheringController(
            GatheringService gatheringService
    ) {
        this.gatheringService = gatheringService;
    }

    @Operation(
            summary = "모임 생성",
            description = """
                    방장이 직접 모임을 생성합니다.

                    생성자는 자동으로 gathering_member에 HOST로 등록되며,
                    current_members는 1(방장)로 시작합니다.

                    태그 ID와 이미지 URL(최대 5장)을 함께 등록할 수 있습니다.
                    존재하지 않는 태그 ID가 포함되면 INVALID_TAG로 거절됩니다.
                    """
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping
    public ResponseEntity<ApiResponse<GatheringCreateResponse>> createGathering(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody GatheringCreateRequest request
    ) {
        String authUserId = jwt.getSubject();

        GatheringCreateResponse response =
                gatheringService.create(authUserId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("모임이 생성되었습니다.", response));
    }

    @Operation(
            summary = "모임 목록 조회",
            description = """
                    카테고리, 상태, 검색어 기준으로 모임 목록을 조회합니다.

                    - category: 모임 카테고리 enum 값(STUDY, HOBBY, HACKATHON, PROJECT, NETWORKING). 미지정 시 전체.
                    - status: 저장 상태 enum 값(RECRUITING, CONFIRMED, COMPLETED, CANCELED). 미지정 시 전체.
                    - keyword: 제목/내용 부분 일치 검색(대소문자 무시).
                    - 정렬은 최신순(created_at DESC) 고정, 페이지네이션은 page=0, size=20 기본.

                    각 항목에는 태그 목록과 화면 표시 상태(displayStatus)가 포함됩니다.
                    잘못된 category/status 값이 오면 400 INVALID_REQUEST 를 반환합니다.
                    """
    )
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<GatheringSummaryResponse>>> getGatherings(
            @Parameter(description = "모임 카테고리 enum 값", example = "HACKATHON") @RequestParam(required = false) String category,
            @Parameter(description = "모임 저장 상태 enum 값", example = "RECRUITING") @RequestParam(required = false) String status,
            @Parameter(description = "제목/내용 검색어") @RequestParam(required = false) String keyword,
            @Parameter(description = "태그 ID 목록(하나라도 가진 모임 매칭, OR)", example = "1,2") @RequestParam(required = false) List<Long> tagIds,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<GatheringSummaryResponse> response =
                gatheringService.getGatherings(category, status, keyword, tagIds, page, size);

        return ResponseEntity.ok(
                ApiResponse.success("모임 목록 조회에 성공했습니다.", response)
        );
    }

    @Operation(
            summary = "모임 상세 조회",
            description = """
                    모임의 상세 정보, 멤버 목록, 태그, 이미지, 내 신청/참여 상태를 조회합니다.

                    - 비로그인 조회를 허용하며, 이 경우 isHost/isMember=false, myApplicationStatus=null 로 반환합니다.
                    - myApplicationStatus 는 신청(application) 도메인 구현 전까지 항상 null 입니다.
                    - 존재하지 않는 모임이면 404 GATHERING_NOT_FOUND 를 반환합니다.
                    """
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{gatheringId}")
    public ResponseEntity<ApiResponse<GatheringDetailResponse>> getGathering(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long gatheringId
    ) {
        String authUserId = (jwt != null) ? jwt.getSubject() : null;

        GatheringDetailResponse response =
                gatheringService.getGatheringDetail(gatheringId, authUserId);

        return ResponseEntity.ok(
                ApiResponse.success("모임 상세 조회에 성공했습니다.", response)
        );
    }

    @Operation(
            summary = "모임 수정",
            description = """
                    모집 중(RECRUITING)인 모임을 방장이 부분 수정합니다.

                    - 보낸 필드만 반영되고, 미전송 필드는 기존 값을 유지합니다.
                    - tagIds/imageUrls 는 목록을 보내면 통째로 교체(빈 목록이면 전체 삭제), 미전송 시 유지.
                    - maxMembers 는 현재 참여 인원보다 적게 줄일 수 없습니다.
                    - 방장이 아니면 403 FORBIDDEN, 모집중이 아니면 409 GATHERING_NOT_MODIFIABLE,
                      없는 모임이면 404 GATHERING_NOT_FOUND.
                    """
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PatchMapping("/{gatheringId}")
    public ResponseEntity<ApiResponse<GatheringUpdateResponse>> updateGathering(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long gatheringId,
            @Valid @RequestBody GatheringUpdateRequest request
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
                    모집 중(RECRUITING)인 모임을 방장이 취소합니다.

                    물리 삭제가 아니라 status = CANCELED 로 변경하고 canceled_at 을 기록합니다.

                    방장이 아니면 403 FORBIDDEN, 모집중이 아니면 409 GATHERING_NOT_MODIFIABLE,
                    없는 모임이면 404 GATHERING_NOT_FOUND.
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
                    방장이 모집을 마감(확정)합니다. status = CONFIRMED 로 변경되고 confirmed_at 을 기록합니다.

                    그룹 채팅방 자동 생성은 채팅 도메인 구축 이후 연계 예정이며,
                    현재 응답의 chatRoomId 는 null 입니다.

                    방장이 아니면 403 FORBIDDEN, 모집중이 아니면 409 GATHERING_NOT_MODIFIABLE,
                    없는 모임이면 404 GATHERING_NOT_FOUND.
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
