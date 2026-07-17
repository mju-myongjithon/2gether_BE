package com.twogether.backend.notification.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 텔레그램 봇 설정. 값은 환경변수(.env)로 주입한다.
 * TELEGRAM_BOT_TOKEN / TELEGRAM_BOT_USERNAME / TELEGRAM_WEBHOOK_SECRET
 */
@Component
public class TelegramProperties {

    private final String botToken;
    private final String botUsername;
    private final String webhookSecret;

    public TelegramProperties(
            @Value("${TELEGRAM_BOT_TOKEN:}") String botToken,
            @Value("${TELEGRAM_BOT_USERNAME:}") String botUsername,
            @Value("${TELEGRAM_WEBHOOK_SECRET:}") String webhookSecret
    ) {
        this.botToken = botToken;
        this.botUsername = botUsername;
        this.webhookSecret = webhookSecret;
    }

    public String getBotToken() {
        return botToken;
    }

    public String getBotUsername() {
        return botUsername;
    }

    public String getWebhookSecret() {
        return webhookSecret;
    }

    public boolean hasWebhookSecret() {
        return webhookSecret != null && !webhookSecret.isBlank();
    }
}
