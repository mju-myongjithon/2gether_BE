package com.twogether.backend.verification.controller;

import com.twogether.backend.global.response.ApiResponse;
import com.twogether.backend.verification.dto.request.VerificationCreateRequest;
import com.twogether.backend.verification.dto.response.VerificationResponse;
import com.twogether.backend.verification.service.VerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "활동 인증 API")
@RestController
@RequestMapping("/api")
public class VerificationController {

    private final VerificationService verificationService;

    public VerificationController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @Operation(summary = "활동 인증 제출")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/gatherings/{gatheringId}/verifications")
    public ResponseEntity<ApiResponse<VerificationResponse>> createVerification(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long gatheringId,
            @Valid @RequestBody VerificationCreateRequest request
    ) {
        VerificationResponse response = verificationService.create(
                jwt.getSubject(), gatheringId, request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("활동 인증이 제출되었습니다.", response));
    }


    @Operation(summary = "AI 활동 인증 판정")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/verifications/{verificationId}/evaluate")
    public ResponseEntity<ApiResponse<VerificationResponse>> evaluateVerification(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long verificationId
    ) {
        VerificationResponse response = verificationService.evaluate(jwt.getSubject(), verificationId);
        return ResponseEntity.ok(ApiResponse.success("AI 활동 인증 판정이 완료되었습니다.", response));
    }
}
