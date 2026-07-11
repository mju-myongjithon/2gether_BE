package com.twogether.backend.department.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "단과대학 및 소속 학과 정보")
public record CollegeResponse(

        @Schema(
                description = "단과대학 고유 ID",
                example = "1"
        )
        Long id,

        @Schema(
                description = "단과대학명",
                example = "반도체·ICT대학"
        )
        String name,

        @Schema(
                description = "해당 단과대학에 소속된 학과 목록"
        )
        List<com.twogether.backend.department.dto.response.DepartmentResponse> departments

) {
}