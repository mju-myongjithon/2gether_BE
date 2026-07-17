package com.twogether.backend.notification.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 텔레그램 Webhook Update 페이로드(필요 필드만). 알 수 없는 필드는 무시한다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TelegramUpdateRequest(
        Message message
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Message(
            Chat chat,
            String text
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Chat(
            Long id
    ) {
    }
}
