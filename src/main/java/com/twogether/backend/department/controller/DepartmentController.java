package com.twogether.backend.department.controller;

import com.twogether.backend.department.dto.response.CollegeResponse;
import com.twogether.backend.department.dto.response.DepartmentResponse;
import com.twogether.backend.department.dto.response.DepartmentTreeResponse;
import com.twogether.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(
        name = "학과 API",
        description = "학번 기준 캠퍼스별 단과대학 및 학과 조회 API"
)
@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    @Operation(
            summary = "학번 기준 단과대학 및 학과 목록 조회",
            description = """
                    사용자가 입력한 학번을 기준으로 캠퍼스를 분류하고,
                    해당 캠퍼스에 소속된 단과대학과 학과 목록을 계층형으로 반환합니다.

                    프론트에서는 별도의 단과대학 선택 버튼을 만들지 않고,
                    하나의 학과 선택창 안에서 단과대학별로 학과를 묶어서 표시할 수 있습니다.

                    사용자가 최종적으로 학과를 선택하면
                    온보딩 API에는 선택한 departmentId만 전달합니다.

                    현재 Swagger 명세 단계에서는 자연캠퍼스 기준 더미 데이터를 반환합니다.
                    실제 캠퍼스 판별 규칙은 추후 서비스 로직에서 적용합니다.
                    """
    )
    @GetMapping
    public ResponseEntity<ApiResponse<DepartmentTreeResponse>>
    getDepartmentsByStudentNumber(

            @Parameter(
                    description = "캠퍼스 분류에 사용할 명지대학교 학번",
                    example = "60231234",
                    required = true
            )
            @RequestParam String studentNumber
    ) {

        List<CollegeResponse> colleges = List.of(
                new CollegeResponse(
                        1L,
                        "반도체·ICT대학",
                        List.of(
                                new DepartmentResponse(1L, "컴퓨터공학과"),
                                new DepartmentResponse(2L, "정보통신공학과"),
                                new DepartmentResponse(3L, "전자공학과")
                        )
                ),
                new CollegeResponse(
                        2L,
                        "공과대학",
                        List.of(
                                new DepartmentResponse(4L, "기계공학과"),
                                new DepartmentResponse(5L, "화학공학과"),
                                new DepartmentResponse(6L, "산업경영공학과")
                        )
                )
        );

        DepartmentTreeResponse response =
                new DepartmentTreeResponse(
                        "NATURAL",
                        colleges
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "단과대학 및 학과 목록 조회에 성공했습니다.",
                        response
                )
        );
    }
}