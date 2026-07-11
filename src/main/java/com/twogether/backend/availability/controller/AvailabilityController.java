package com.twogether.backend.availability.controller;

import com.twogether.backend.availability.domain.AvailabilityRepeatType;
import com.twogether.backend.availability.dto.response.AvailabilityResponse;
import com.twogether.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.twogether.backend.availability.dto.request.AvailabilityUpdateRequest;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Tag(
        name = "가용 일정 API",
        description = "사용자가 모임에 참여할 수 있는 시간 관리 API"
)
@RestController
@RequestMapping("/api/users/me/availabilities")
public class AvailabilityController {

    @Operation(
            summary = "내 가용 일정 조회",
            description = """
                    현재 사용자가 등록한 가용 일정 목록을 조회합니다.
                    일정은 30분 단위로 등록하며,
                    매주·홀수 주·짝수 주 반복을 지원합니다.
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

                예시:
                - 기존 월·수·금에서 수요일을 해제하고 월·금만 보내면 수요일 일정이 삭제됩니다.
                - 빈 배열을 보내면 기존 가용 일정이 모두 삭제됩니다.
                - 시간은 30분 단위로 전달합니다.
                - 반복 유형은 EVERY_WEEK, ODD_WEEK, EVEN_WEEK를 사용합니다.
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