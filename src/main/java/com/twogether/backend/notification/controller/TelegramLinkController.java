package com.twogether.backend.notification.controller;

import com.twogether.backend.global.response.ApiResponse;
import com.twogether.backend.notification.dto.response.TelegramLinkResponse;
import com.twogether.backend.notification.dto.response.TelegramStatusResponse;
import com.twogether.backend.notification.service.TelegramLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "텔레그램 연결 API",
        description = "알림 수신용 텔레그램 봇 연결/상태/해제 API"
)
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/me/telegram")
public class TelegramLinkController {

    private final TelegramLinkService telegramLinkService;

    public TelegramLinkController(TelegramLinkService telegramLinkService) {
        this.telegramLinkService = telegramLinkService;
    }

    @Operation(
            summary = "텔레그램 연결 딥링크 발급",
            description = """
                    봇 연결용 딥링크(t.me/bot?start=code)와 일회성 코드를 발급합니다.
                    사용자가 딥링크를 눌러 봇을 시작(/start)하면 연결이 완료됩니다.
                    """
    )
    @PostMapping("/link-code")
    public ResponseEntity<ApiResponse<TelegramLinkResponse>> issueLinkCode(
            @AuthenticationPrincipal Jwt jwt
    ) {
        TelegramLinkResponse response =
                telegramLinkService.issueLinkCode(jwt.getSubject());

        return ResponseEntity.ok(
                ApiResponse.success("텔레그램 연결 딥링크를 발급했습니다.", response)
        );
    }

    @Operation(
            summary = "텔레그램 연결 상태 조회",
            description = "내 텔레그램 연결 여부와 연결 정보를 조회합니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<TelegramStatusResponse>> getStatus(
            @AuthenticationPrincipal Jwt jwt
    ) {
        TelegramStatusResponse response =
                telegramLinkService.getStatus(jwt.getSubject());

        return ResponseEntity.ok(
                ApiResponse.success("텔레그램 연결 상태 조회에 성공했습니다.", response)
        );
    }

    @Operation(
            summary = "텔레그램 연결 해제",
            description = "텔레그램 연결을 해제합니다. 미연결 상태에서도 정상 처리됩니다."
    )
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> unlink(
            @AuthenticationPrincipal Jwt jwt
    ) {
        telegramLinkService.unlink(jwt.getSubject());

        return ResponseEntity.ok(
                ApiResponse.success("텔레그램 연결을 해제했습니다.")
        );
    }
}
