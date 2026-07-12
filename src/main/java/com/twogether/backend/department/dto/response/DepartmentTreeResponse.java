package com.twogether.backend.department.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "캠퍼스별 단과대학 및 학과 목록 응답")
public record DepartmentTreeResponse(

        @Schema(
                description = "학번을 기준으로 분류된 캠퍼스",
                example = "NATURAL",
                allowableValues = {"HUMANITIES", "NATURAL"}
        )
        String campus,

        @Schema(
                description = "해당 캠퍼스의 단과대학 및 학과 목록"
        )
        List<CollegeResponse> colleges

) {
}