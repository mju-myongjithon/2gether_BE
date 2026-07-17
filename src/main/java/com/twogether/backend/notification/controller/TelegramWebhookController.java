package com.twogether.backend.notification.controller;

import com.twogether.backend.notification.config.TelegramProperties;
import com.twogether.backend.notification.dto.request.TelegramUpdateRequest;
import com.twogether.backend.notification.service.TelegramLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 텔레그램 Webhook 수신 엔드포인트.
 *
 * ⚠️ 텔레그램은 우리 JWT를 갖지 않으므로 이 경로는 SecurityConfig 에서 permitAll 로 열어야 한다
 *    (예: HttpMethod.POST, "/api/telegram/webhook"). SecurityConfig 는 global 이라 별도 반영 필요.
 * 위조 방지: setWebhook 시 지정한 secret 을 X-Telegram-Bot-Api-Secret-Token 헤더로 검증한다.
 */
@Tag(
        name = "텔레그램 Webhook",
        description = "텔레그램 봇 업데이트 수신(내부/텔레그램 전용)"
)
@RestController
@RequestMapping("/api/telegram")
public class TelegramWebhookController {

    private final TelegramLinkService telegramLinkService;
    private final TelegramProperties telegramProperties;

    public TelegramWebhookController(
            TelegramLinkService telegramLinkService,
            TelegramProperties telegramProperties
    ) {
        this.telegramLinkService = telegramLinkService;
        this.telegramProperties = telegramProperties;
    }

    @Operation(
            summary = "텔레그램 Webhook 수신",
            description = "텔레그램이 봇 업데이트를 POST 하는 엔드포인트. /start {code} 를 파싱해 연결을 처리합니다."
    )
    @PostMapping("/webhook")
    public ResponseEntity<Void> receive(
            @RequestHeader(value = "X-Telegram-Bot-Api-Secret-Token", required = false) String secretToken,
            @RequestBody TelegramUpdateRequest update
    ) {
        // secret 이 설정돼 있으면 검증. 불일치 시 처리하지 않고 200 반환(재전송 폭주 방지).
        if (telegramProperties.hasWebhookSecret()
                && !telegramProperties.getWebhookSecret().equals(secretToken)) {
            return ResponseEntity.ok().build();
        }

        TelegramUpdateRequest.Message message = update.message();
        if (message != null && message.text() != null
                && message.chat() != null && message.chat().id() != null) {
            telegramLinkService.handleStartCommand(message.chat().id(), message.text());
        }

        return ResponseEntity.ok().build();
    }
}
