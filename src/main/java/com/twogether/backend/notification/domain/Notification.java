package com.twogether.backend.notification.domain;

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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * 이벤트성 인앱 알림(벨).
 *
 * 채팅 메시지 배지는 여기 쌓지 않고(폭주 방지) chat_room_member.last_read_message_id 로 계산한다.
 * 각 도메인은 이벤트만 발행 → NotificationService 가 저장/전송(결합도 0).
 *
 * - title/content: 발생 당시 텍스트(불변).
 * - meta: 이동 대상({gatheringId} / {roomId, messageId} 등) JSONB.
 * - readAt: NULL 이면 안읽음 → 벨 배지.
 */
@Entity
@Table(
        name = "notification",
        indexes = {
                @Index(name = "idx_notification_user_created", columnList = "user_id, created_at"),
                @Index(name = "idx_notification_user_read", columnList = "user_id, read_at")
        }
)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "type",
            nullable = false,
            length = 40
    )
    private NotificationType type;

    @Column(
            name = "title",
            nullable = false,
            length = 120
    )
    private String title;

    @Column(
            name = "content",
            length = 300
    )
    private String content;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
            name = "meta",
            columnDefinition = "jsonb"
    )
    private Map<String, Object> meta;

    @Column(name = "read_at")
    private OffsetDateTime readAt;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;

    protected Notification() {
    }

    private Notification(
            User user,
            NotificationType type,
            String title,
            String content,
            Map<String, Object> meta
    ) {
        this.user = user;
        this.type = type;
        this.title = title;
        this.content = content;
        this.meta = meta;
    }

    public static Notification of(
            User user,
            NotificationType type,
            String title,
            String content,
            Map<String, Object> meta
    ) {
        return new Notification(user, type, title, content, meta);
    }

    @PrePersist
    private void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    public void markRead() {
        if (this.readAt == null) {
            this.readAt = OffsetDateTime.now();
        }
    }

    public boolean isRead() {
        return this.readAt != null;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public NotificationType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public Map<String, Object> getMeta() {
        return meta;
    }

    public OffsetDateTime getReadAt() {
        return readAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
