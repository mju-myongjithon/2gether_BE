package com.twogether.backend.availability.controller;

import com.twogether.backend.availability.dto.request.AvailabilityUpdateRequest;
import com.twogether.backend.availability.dto.response.AvailabilityResponse;
import com.twogether.backend.availability.service.AvailabilityService;
import com.twogether.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(
        name = "가용 일정 API",
        description = "사용자가 모임에 참여할 수 있는 시간 관리 API"
)
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/users")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(
            AvailabilityService availabilityService
    ) {
        this.availabilityService = availabilityService;
    }

    @Operation(
            summary = "내 가용 일정 조회",
            description = """
                    현재 로그인한 사용자가 등록한 가용 일정 목록을 조회합니다.

                    프론트에서는 이 응답을 받아 Touch UI 캘린더의
                    기존 선택 시간대를 색칠할 수 있습니다.

                    일정은 30분 단위로 사용하며,
                    매주·홀수 주·짝수 주 반복을 지원합니다.

                    반복 유형:
                    - EVERY_WEEK: 매주
                    - ODD_WEEK: 홀수 주
                    - EVEN_WEEK: 짝수 주
                    """
    )
    @GetMapping("/me/availabilities")
    public ResponseEntity<ApiResponse<List<AvailabilityResponse>>>
    getMyAvailabilities(
            @AuthenticationPrincipal Jwt jwt
    ) {
        String authUserId = jwt.getSubject();

        List<AvailabilityResponse> availabilities =
                availabilityService.getMyAvailabilities(
                        authUserId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "가용 일정 조회에 성공했습니다.",
                        availabilities
                )
        );
    }

    @Operation(
            summary = "타 사용자 가용 일정 조회",
            description = """
                    사용자 ID를 기준으로 타 사용자가 등록한
                    가용 일정 목록을 조회합니다.

                    타 사용자 프로필 화면에서 해당 사용자의 프로필,
                    관심사 태그, 스킬 태그와 함께 가용 일정을 노출할 때 사용합니다.

                    타 사용자 일정은 읽기 전용이며 수정할 수 없습니다.

                    일정은 30분 단위로 사용하며,
                    매주·홀수 주·짝수 주 반복을 지원합니다.

                    반복 유형:
                    - EVERY_WEEK: 매주
                    - ODD_WEEK: 홀수 주
                    - EVEN_WEEK: 짝수 주

                    사용자는 존재하지만 등록한 가용 일정이 없으면
                    빈 배열을 반환합니다.
                    """
    )
    @GetMapping("/{userId}/availabilities")
    public ResponseEntity<ApiResponse<List<AvailabilityResponse>>>
    getUserAvailabilities(
            @Parameter(
                    description = "가용 일정을 조회할 사용자의 고유 ID",
                    example = "1",
                    required = true
            )
            @PathVariable Long userId
    ) {
        List<AvailabilityResponse> availabilities =
                availabilityService.getUserAvailabilities(
                        userId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "사용자 가용 일정 조회에 성공했습니다.",
                        availabilities
                )
        );
    }

    @Operation(
            summary = "내 가용 일정 전체 저장 및 재설정",
            description = """
                    Touch UI에서 최종 선택된 가용 일정 전체를 저장합니다.

                    기존 일정은 요청으로 전달된 목록으로 전체 교체됩니다.

                    사용 예시:
                    - 기존 월·수·금에서 수요일을 해제하고 월·금만 보내면 수요일 일정이 삭제됩니다.
                    - 모든 칸을 선택 해제한 뒤 빈 배열을 보내면 기존 일정이 모두 삭제됩니다.
                    - 기존 월·수·금을 화·목으로 바꿔 보내면 전체 일정이 화·목으로 재설정됩니다.

                    시간은 30분 단위로 전달합니다.

                    반복 유형:
                    - EVERY_WEEK: 매주
                    - ODD_WEEK: 홀수 주
                    - EVEN_WEEK: 짝수 주
                    """
    )
    @PutMapping("/me/availabilities")
    public ResponseEntity<ApiResponse<Void>>
    updateMyAvailabilities(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AvailabilityUpdateRequest request
    ) {
        String authUserId = jwt.getSubject();

        availabilityService.updateMyAvailabilities(
                authUserId,
                request
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "가용 일정 수정에 성공했습니다."
                )
        );
    }
}