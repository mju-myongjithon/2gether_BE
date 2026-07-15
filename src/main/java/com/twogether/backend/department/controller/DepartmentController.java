package com.twogether.backend.department.controller;

import com.twogether.backend.department.dto.response.DepartmentResponse;
import com.twogether.backend.department.service.DepartmentService;
import com.twogether.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(
        name = "학과 API",
        description = "전체 학과 목록 조회 API"
)
@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(
            DepartmentService departmentService
    ) {
        this.departmentService = departmentService;
    }

    @Operation(
            summary = "전체 학과 목록 조회",
            description = """
                    전체 학과 목록을 반환합니다.

                    각 학과에는 id, name, campus가 포함됩니다.

                    프론트에서는 학과 목록을 원하는 단과대학 기준으로
                    분류해서 화면에 표시할 수 있습니다.

                    사용자가 학과를 선택하면 해당 학과의 id를
                    기존 온보딩 API의 departmentId로 전달합니다.

                    학번은 사용자 정보로만 저장하며,
                    캠퍼스 분류에는 사용하지 않습니다.
                    """
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>>
    getDepartments() {

        List<DepartmentResponse> response =
                departmentService.getDepartments();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "학과 목록 조회에 성공했습니다.",
                        response
                )
        );
    }
}