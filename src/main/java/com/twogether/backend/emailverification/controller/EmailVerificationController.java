package com.twogether.backend.emailverification.controller;

import com.twogether.backend.emailverification.dto.request.EmailVerificationConfirmRequest;
import com.twogether.backend.emailverification.dto.request.EmailVerificationSendRequest;
import com.twogether.backend.emailverification.service.EmailVerificationService;
import com.twogether.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "학교 이메일 인증 API",
        description = "명지대학교 이메일 인증 관련 API"
)
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/email-verifications")
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    public EmailVerificationController(
            EmailVerificationService emailVerificationService
    ) {
        this.emailVerificationService = emailVerificationService;
    }

    @Operation(
            summary = "학교 이메일 인증번호 발송",
            description = """
                    로그인한 사용자가 입력한 명지대학교 이메일로
                    6자리 인증번호를 발송합니다.

                    인증번호는 5분 동안 유효합니다.
                    """
    )
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> sendVerificationCode(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody EmailVerificationSendRequest request
    ) {
        emailVerificationService.sendVerificationCode(
                jwt.getSubject(),
                request.email()
        );

        return ResponseEntity.ok(
                ApiResponse.success("인증번호 발송에 성공했습니다.")
        );
    }

    @Operation(
            summary = "학교 이메일 인증번호 확인",
            description = """
                    입력한 학교 이메일과 인증번호를 확인합니다.

                    인증 성공 시 사용자의 학교 이메일과
                    이메일 인증 완료 상태가 저장됩니다.
                    """
    )
    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmVerificationCode(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody EmailVerificationConfirmRequest request
    ) {
        emailVerificationService.confirmVerificationCode(
                jwt.getSubject(),
                request.email(),
                request.code()
        );

        return ResponseEntity.ok(
                ApiResponse.success("학교 이메일 인증에 성공했습니다.")
        );
    }
}