package com.twogether.backend.availability.dto.response;

import com.twogether.backend.availability.domain.AvailabilityRepeatType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Schema(description = "사용자 가용 일정 응답")
public record AvailabilityResponse(

        @Schema(
                description = "가용 일정 고유 ID",
                example = "1"
        )
        Long id,

        @Schema(
                description = "일정 반복 유형",
                example = "EVERY_WEEK",
                allowableValues = {
                        "EVERY_WEEK",
                        "ODD_WEEK",
                        "EVEN_WEEK"
                }
        )
        AvailabilityRepeatType repeatType,

        @Schema(
                description = "요일",
                example = "MONDAY"
        )
        DayOfWeek dayOfWeek,

        @Schema(
                description = "가능 시작 시간이며 30분 단위로 사용합니다.",
                example = "18:00"
        )
        LocalTime startTime,

        @Schema(
                description = "가능 종료 시간이며 30분 단위로 사용합니다.",
                example = "20:00"
        )
        LocalTime endTime

) {
}