package com.twogether.backend.chat.domain;

import com.twogether.backend.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.OffsetDateTime;

/**
 * 안심 커넥트(Quick Connect) 코드.
 *
 * - chatRoom: QUICK_CONNECT 방과 1:1(유니크 소프트 참조 → 여기서는 관계 매핑, unique).
 * - code: 6자리 코드. 활성 코드 유일성은 부분 유니크(WHERE status=ACTIVE)로 DDL 보장해야 하며,
 *   ddl-auto=update 로는 생성되지 않으므로 마이그레이션에서 추가한다. 여기서는 앱 레벨로 보장.
 * - status: ACTIVE / EXPIRED / USED.
 */
@Entity
@Table(
        name = "quick_connect_code",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_quick_connect_code_room", columnNames = "chat_room_id")
        },
        indexes = {
                @Index(name = "idx_quick_connect_code_code", columnList = "code")
        }
)
public class QuickConnectCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "chat_room_id",
            nullable = false
    )
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(
            name = "code",
            nullable = false,
            length = 6
    )
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private QuickCodeStatus status;

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

    protected QuickConnectCode() {
    }

    public QuickConnectCode(
            ChatRoom chatRoom,
            User createdBy,
            String code,
            OffsetDateTime expiresAt
    ) {
        this.chatRoom = chatRoom;
        this.createdBy = createdBy;
        this.code = code;
        this.expiresAt = expiresAt;
        this.status = QuickCodeStatus.ACTIVE;
    }

    @PrePersist
    private void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    public boolean isActive() {
        return this.status == QuickCodeStatus.ACTIVE;
    }

    public boolean isExpired(OffsetDateTime now) {
        return this.expiresAt.isBefore(now);
    }

    public void markExpired() {
        this.status = QuickCodeStatus.EXPIRED;
    }

    public void markUsed() {
        this.status = QuickCodeStatus.USED;
    }

    public Long getId() {
        return id;
    }

    public ChatRoom getChatRoom() {
        return chatRoom;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public String getCode() {
        return code;
    }

    public QuickCodeStatus getStatus() {
        return status;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
