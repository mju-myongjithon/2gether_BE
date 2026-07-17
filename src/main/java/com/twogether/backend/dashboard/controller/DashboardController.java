package com.twogether.backend.dashboard.controller;

import com.twogether.backend.dashboard.dto.response.DashboardStatisticsResponse;
import com.twogether.backend.dashboard.service.DashboardService;
import com.twogether.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Dashboard API")
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;
    public DashboardController(DashboardService dashboardService) { this.dashboardService = dashboardService; }

    @Operation(summary = "이번 달 교류·매칭 그룹·참여 학우 통계 조회")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<DashboardStatisticsResponse>> statistics() {
        return ResponseEntity.ok(ApiResponse.success("라이브 유대감 통계 조회에 성공했습니다.", dashboardService.getStatistics()));
    }
}
