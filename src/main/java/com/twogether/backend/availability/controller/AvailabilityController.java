package com.twogether.backend.availability.controller;

import com.twogether.backend.availability.domain.AvailabilityRepeatType;
import com.twogether.backend.availability.dto.request.AvailabilityUpdateRequest;
import com.twogether.backend.availability.dto.response.AvailabilityResponse;
import com.twogether.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Tag(
        name = "가용 일정 API",
        description = "사용자가 모임에 참여할 수 있는 시간 관리 API"
)
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/users/me/availabilities")
public class AvailabilityController {

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

                    현재 Swagger 명세 단계에서는 더미 일정을 반환합니다.
                    """
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<AvailabilityResponse>>>
    getMyAvailabilities() {

        List<AvailabilityResponse> availabilities = List.of(
                new AvailabilityResponse(
                        1L,
                        AvailabilityRepeatType.EVERY_WEEK,
                        DayOfWeek.MONDAY,
                        LocalTime.of(18, 0),
                        LocalTime.of(20, 0)
                ),
                new AvailabilityResponse(
                        2L,
                        AvailabilityRepeatType.ODD_WEEK,
                        DayOfWeek.WEDNESDAY,
                        LocalTime.of(19, 30),
                        LocalTime.of(21, 0)
                ),
                new AvailabilityResponse(
                        3L,
                        AvailabilityRepeatType.EVEN_WEEK,
                        DayOfWeek.FRIDAY,
                        LocalTime.of(14, 0),
                        LocalTime.of(16, 30)
                )
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "가용 일정 조회에 성공했습니다.",
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

                    현재 Swagger 명세 단계에서는 실제 DB에 저장하지 않고
                    성공 응답만 반환합니다.
                    """
    )
    @PutMapping
    public ResponseEntity<ApiResponse<Void>> updateMyAvailabilities(
            @RequestBody AvailabilityUpdateRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("가용 일정 수정에 성공했습니다.")
        );
    }
}