package com.twogether.backend.availability.dto.request;

import com.twogether.backend.availability.domain.AvailabilityRepeatType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Schema(description = "가용 시간대 요청")
public record AvailabilitySlotRequest(

        @NotNull(message = "반복 유형은 필수입니다.")
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

        @NotNull(message = "요일은 필수입니다.")
        @Schema(
                description = "요일",
                example = "MONDAY"
        )
        DayOfWeek dayOfWeek,

        @NotNull(message = "시작 시간은 필수입니다.")
        @Schema(
                description = "가능 시작 시간",
                example = "18:00"
        )
        LocalTime startTime,

        @NotNull(message = "종료 시간은 필수입니다.")
        @Schema(
                description = "가능 종료 시간",
                example = "20:00"
        )
        LocalTime endTime

) {
}