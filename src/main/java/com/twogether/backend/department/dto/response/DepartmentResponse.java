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
                example = "컴퓨터공학과"
        )
        String name

) {
}