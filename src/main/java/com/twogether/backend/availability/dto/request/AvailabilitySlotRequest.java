package com.twogether.backend.availability.dto.request;

import com.twogether.backend.availability.domain.AvailabilityRepeatType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Schema(description = "가용 시간대 요청")
public record AvailabilitySlotRequest(

        @Schema(
                description = "반복 유형",
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
                description = "가능 시작 시간",
                example = "18:00"
        )
        LocalTime startTime,

        @Schema(
                description = "가능 종료 시간",
                example = "20:00"
        )
        LocalTime endTime

) {
}