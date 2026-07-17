package com.twogether.backend.notification.telegram;

import com.twogether.backend.notification.config.TelegramProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * 텔레그램 Bot API 발송기. 실패가 채팅/알림 흐름을 막지 않도록 예외를 삼키고 로깅만 한다.
 * 봇 토큰 미설정이면 발송을 조용히 스킵한다.
 */
@Component
public class TelegramSender {

    private static final Logger log = LoggerFactory.getLogger(TelegramSender.class);

    private final TelegramProperties telegramProperties;
    private final RestClient restClient = RestClient.create();

    public TelegramSender(TelegramProperties telegramProperties) {
        this.telegramProperties = telegramProperties;
    }

    public void send(Long chatId, String text) {
        String token = telegramProperties.getBotToken();
        if (token == null || token.isBlank()) {
            log.debug("텔레그램 봇 토큰 미설정 → 발송 스킵 (chatId={})", chatId);
            return;
        }
        try {
            restClient.post()
                    .uri("https://api.telegram.org/bot" + token + "/sendMessage")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("chat_id", chatId, "text", text))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("텔레그램 발송 실패 (chatId={}): {}", chatId, e.getMessage());
        }
    }
}
