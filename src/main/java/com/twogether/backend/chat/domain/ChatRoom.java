package com.twogether.backend.chat.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.OffsetDateTime;

/**
 * 채팅방 메타.
 *
 * - type: GROUP(모임 확정 시 자동 생성) / DIRECT(1:1) / QUICK_CONNECT(안심 커넥트)
 * - gatheringId: 모임 자동 생성 방과의 1:1 소프트 참조(FK 관계 매핑 아님, 유니크). DIRECT·QUICK_CONNECT는 NULL.
 * - lastMessageId / lastMessageAt: 방 목록 최근순 정렬·미리보기용 비정규화 캐시(소프트 참조).
 * - closedAt: QUICK_CONNECT 종료 시각.
 */
@Entity
@Table(
        name = "chat_room",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_chat_room_gathering_id", columnNames = "gathering_id")
        }
)
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "type",
            nullable = false,
            length = 20
    )
    private ChatRoomType type;

    @Column(name = "gathering_id")
    private Long gatheringId;

    @Column(
            name = "title",
            length = 120
    )
    private String title;

    @Column(name = "last_message_id")
    private Long lastMessageId;

    @Column(name = "last_message_at")
    private OffsetDateTime lastMessageAt;

    @Column(name = "closed_at")
    private OffsetDateTime closedAt;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;

    protected ChatRoom() {
    }

    private ChatRoom(
            ChatRoomType type,
            Long gatheringId,
            String title
    ) {
        this.type = type;
        this.gatheringId = gatheringId;
        this.title = title;
    }

    public static ChatRoom group(
            Long gatheringId,
            String title
    ) {
        return new ChatRoom(ChatRoomType.GROUP, gatheringId, title);
    }

    public static ChatRoom direct(
            String title
    ) {
        return new ChatRoom(ChatRoomType.DIRECT, null, title);
    }

    public static ChatRoom quickConnect(
            String title
    ) {
        return new ChatRoom(ChatRoomType.QUICK_CONNECT, null, title);
    }

    @PrePersist
    private void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    /**
     * 새 메시지 저장 시 방 목록 미리보기/정렬용 캐시를 갱신한다.
     */
    public void updateLastMessage(
            Long lastMessageId,
            OffsetDateTime lastMessageAt
    ) {
        this.lastMessageId = lastMessageId;
        this.lastMessageAt = lastMessageAt;
    }

    public void close() {
        this.closedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public ChatRoomType getType() {
        return type;
    }

    public Long getGatheringId() {
        return gatheringId;
    }

    public String getTitle() {
        return title;
    }

    public Long getLastMessageId() {
        return lastMessageId;
    }

    public OffsetDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public OffsetDateTime getClosedAt() {
        return closedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
