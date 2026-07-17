package com.twogether.backend.chat.controller;

import com.twogether.backend.chat.dto.request.QuickConnectJoinRequest;
import com.twogether.backend.chat.dto.response.QuickConnectCreateResponse;
import com.twogether.backend.chat.dto.response.QuickConnectJoinResponse;
import com.twogether.backend.chat.service.QuickConnectService;
import com.twogether.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "안심 커넥트 API",
        description = "6자리 코드로 입장하는 즉석 채팅방(모임 무관) 생성/입장 API"
)
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/quick-connect")
public class QuickConnectController {

    private final QuickConnectService quickConnectService;

    public QuickConnectController(QuickConnectService quickConnectService) {
        this.quickConnectService = quickConnectService;
    }

    @Operation(
            summary = "안심 커넥트 방 생성",
            description = """
                    즉석 채팅방(type=QUICK_CONNECT)과 6자리 입장 코드를 생성합니다.
                    생성자는 방장(OWNER)으로 입장하며, 코드는 만료 시각을 가집니다.
                    """
    )
    @PostMapping
    public ResponseEntity<ApiResponse<QuickConnectCreateResponse>> create(
            @AuthenticationPrincipal Jwt jwt
    ) {
        QuickConnectCreateResponse response =
                quickConnectService.create(jwt.getSubject());

        return ResponseEntity.ok(
                ApiResponse.success("안심 커넥트 방이 생성되었습니다.", response)
        );
    }

    @Operation(
            summary = "안심 커넥트 코드로 입장",
            description = """
                    6자리 코드를 검증(ACTIVE/만료/사용)한 뒤 방에 입장합니다.
                    코드는 일회성으로, 입장 성공 시 사용 완료(USED) 처리됩니다.
                    """
    )
    @PostMapping("/join")
    public ResponseEntity<ApiResponse<QuickConnectJoinResponse>> join(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody QuickConnectJoinRequest request
    ) {
        QuickConnectJoinResponse response =
                quickConnectService.join(jwt.getSubject(), request.code());

        return ResponseEntity.ok(
                ApiResponse.success("안심 커넥트 방에 입장했습니다.", response)
        );
    }
}
