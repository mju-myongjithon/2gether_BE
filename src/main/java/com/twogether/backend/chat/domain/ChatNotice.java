package com.twogether.backend.chat.domain;

import com.twogether.backend.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/**
 * 채팅방 공지(상단 고정).
 *
 * - messageId: 공지로 고정한 메시지 참조(선택, 소프트 참조 Long).
 * - createdBy: role=OWNER 인 멤버.
 * - isActive: 방당 활성 공지 1건. 활성 공지 유일성은 부분 유니크(WHERE is_active)로 DDL 보장.
 *   (ddl-auto=update 로는 부분 유니크가 생성되지 않으므로 별도 마이그레이션에서 처리 — C7에서 다룸)
 */
@Entity
@Table(
        name = "chat_notice",
        indexes = {
                @Index(name = "idx_chat_notice_room_active", columnList = "chat_room_id, is_active")
        }
)
public class ChatNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "chat_room_id",
            nullable = false
    )
    private ChatRoom chatRoom;

    @Column(name = "message_id")
    private Long messageId;

    @Column(
            name = "content",
            nullable = false,
            length = 500
    )
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "created_by",
            nullable = false
    )
    private User createdBy;

    @Column(
            name = "is_active",
            nullable = false
    )
    private boolean isActive;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;

    protected ChatNotice() {
    }

    public ChatNotice(
            ChatRoom chatRoom,
            Long messageId,
            String content,
            User createdBy
    ) {
        this.chatRoom = chatRoom;
        this.messageId = messageId;
        this.content = content;
        this.createdBy = createdBy;
        this.isActive = true;
    }

    @PrePersist
    private void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    public void deactivate() {
        this.isActive = false;
    }

    public Long getId() {
        return id;
    }

    public ChatRoom getChatRoom() {
        return chatRoom;
    }

    public Long getMessageId() {
        return messageId;
    }

    public String getContent() {
        return content;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public boolean isActive() {
        return isActive;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
