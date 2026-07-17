package com.twogether.backend.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.OffsetDateTime;

/**
 * 텔레그램 연결용 일회성 코드. 딥링크(t.me/bot?start={code})로 봇 /start 시 이 코드로 사용자를 식별한다.
 * (ERD 외 구현 보조 테이블)
 */
@Entity
@Table(
        name = "telegram_link_code",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_telegram_link_code_code", columnNames = "code")
        }
)
public class TelegramLinkCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "user_id",
            nullable = false
    )
    private Long userId;

    @Column(
            name = "code",
            nullable = false,
            length = 64
    )
    private String code;

    @Column(
            name = "used",
            nullable = false
    )
    private boolean used;

    @Column(
            name = "expires_at",
            nullable = false
    )
    private OffsetDateTime expiresAt;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;

    protected TelegramLinkCode() {
    }

    public TelegramLinkCode(
            Long userId,
            String code,
            OffsetDateTime expiresAt
    ) {
        this.userId = userId;
        this.code = code;
        this.expiresAt = expiresAt;
        this.used = false;
    }

    @PrePersist
    private void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    public boolean isUsable(OffsetDateTime now) {
        return !this.used && this.expiresAt.isAfter(now);
    }

    public void markUsed() {
        this.used = true;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getCode() {
        return code;
    }

    public boolean isUsed() {
        return used;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
