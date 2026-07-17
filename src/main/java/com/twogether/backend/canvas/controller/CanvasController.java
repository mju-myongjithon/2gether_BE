package com.twogether.backend.canvas.controller;

import com.twogether.backend.canvas.dto.response.CanvasContributionResponse;
import com.twogether.backend.canvas.dto.response.CanvasDailyActivitiesResponse;
import com.twogether.backend.canvas.service.CanvasService;
import com.twogether.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@Tag(name = "Campus Canvas API")
@RestController
@RequestMapping("/api/canvas")
public class CanvasController {
    private final CanvasService canvasService;
    public CanvasController(CanvasService canvasService) { this.canvasService = canvasService; }

    @Operation(summary = "최근 1년간 AI 승인 활동을 GitHub 잔디 형태로 조회")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/contributions")
    public ResponseEntity<ApiResponse<CanvasContributionResponse>> contributions() {
        return ResponseEntity.ok(ApiResponse.success("캠퍼스 잔디 조회에 성공했습니다.", canvasService.getContributions()));
    }

    @Operation(summary = "선택한 날짜의 AI 승인 활동 상세 조회")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/activities")
    public ResponseEntity<ApiResponse<CanvasDailyActivitiesResponse>> activities(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success("해당 날짜의 활동 내역 조회에 성공했습니다.", canvasService.getActivities(date)));
    }
}
