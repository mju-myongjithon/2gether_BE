package com.twogether.backend.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.OffsetDateTime;

/**
 * 사용자 ↔ 텔레그램 연결 매핑(알림 발송용). 사용자:텔레그램 = 1:1.
 *
 * user_id 를 PK로 사용(공유 기본키). telegram_chat_id 는 봇이 메시지를 보낼 대상.
 * 텔레그램 봇은 사용자가 먼저 /start 해야만 발송 가능하므로, 연결된 사용자에게만 텔레그램 알림을 보낸다.
 */
@Entity
@Table(
        name = "user_telegram",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_telegram_chat_id", columnNames = "telegram_chat_id")
        }
)
public class UserTelegram {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(
            name = "telegram_chat_id",
            nullable = false
    )
    private Long telegramChatId;

    @Column(
            name = "linked_at",
            nullable = false
    )
    private OffsetDateTime linkedAt;

    protected UserTelegram() {
    }

    public UserTelegram(
            Long userId,
            Long telegramChatId
    ) {
        this.userId = userId;
        this.telegramChatId = telegramChatId;
    }

    @PrePersist
    private void onCreate() {
        this.linkedAt = OffsetDateTime.now();
    }

    /**
     * 재연결(다른 텔레그램 계정으로 갱신).
     */
    public void relink(Long telegramChatId) {
        this.telegramChatId = telegramChatId;
        this.linkedAt = OffsetDateTime.now();
    }

    public Long getUserId() {
        return userId;
    }

    public Long getTelegramChatId() {
        return telegramChatId;
    }

    public OffsetDateTime getLinkedAt() {
        return linkedAt;
    }
}
