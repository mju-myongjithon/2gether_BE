package com.twogether.backend.department.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "학과 정보")
public record DepartmentResponse(

        @Schema(
                description = "학과 고유 ID",
                example = "1"
        )
        Long id,

        @Schema(
                description = "학과명",
                example = "컴퓨터정보통신공학부"
        )
        String name,

        @Schema(
                description = "학과가 소속된 캠퍼스",
                example = "NATURAL",
                allowableValues = {
                        "HUMANITIES",
                        "NATURAL"
                }
        )
        String campus

) {
}