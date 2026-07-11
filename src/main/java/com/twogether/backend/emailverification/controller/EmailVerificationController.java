package com.twogether.backend.emailverification.controller;

import com.twogether.backend.emailverification.dto.request.EmailVerificationConfirmRequest;
import com.twogether.backend.emailverification.dto.request.EmailVerificationSendRequest;
import com.twogether.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "학교 이메일 인증 API",
        description = "명지대학교 이메일 인증 관련 API"
)
@RestController
@RequestMapping("/api/email-verifications")
public class EmailVerificationController {

    @Operation(
            summary = "학교 이메일 인증번호 발송",
            description = """
                    입력받은 명지대학교 이메일로 인증번호 발송을 요청합니다.
                    현재 Swagger 명세 단계에서는 실제 이메일을 발송하지 않습니다.
                    """
    )
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> sendVerificationCode(
            @RequestBody EmailVerificationSendRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("인증번호 발송에 성공했습니다.")
        );
    }

    @Operation(
            summary = "학교 이메일 인증번호 확인",
            description = """
                    사용자가 입력한 학교 이메일과 인증번호를 확인합니다.
                    인증에 성공하면 해당 학교 이메일의 인증이 완료된 것으로 처리합니다.

                    현재 Swagger 명세 단계에서는 실제 인증번호를 검증하지 않고
                    성공 응답을 반환합니다.
                    """
    )
    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmVerificationCode(
            @RequestBody EmailVerificationConfirmRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("학교 이메일 인증에 성공했습니다.")
        );
    }
}