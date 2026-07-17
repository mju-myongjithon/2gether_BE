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
 * 채팅방 참여자.
 *
 * - role: OWNER(공지 등 권한) / MEMBER
 * - lastReadMessageId: 안읽음 수 = 이 값보다 큰 message 개수(소프트 참조, Long 캐시)
 * - notificationEnabled: 방별 알림 on/off(뮤트). 알림 발송(N3)에서 참조.
 * - leftAt: 나가기(소프트). NULL이면 참여 중.
 */
@Entity
@Table(
        name = "chat_room_member",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_chat_room_member_room_user",
                        columnNames = {"chat_room_id", "user_id"}
                )
        },
        indexes = {
                @Index(name = "idx_chat_room_member_user_id", columnList = "user_id")
        }
)
public class ChatRoomMember {

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
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "role",
            nullable = false,
            length = 20
    )
    private ChatMemberRole role;

    @Column(name = "last_read_message_id")
    private Long lastReadMessageId;

    @Column(
            name = "notification_enabled",
            nullable = false
    )
    private boolean notificationEnabled;

    @Column(
            name = "joined_at",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime joinedAt;

    @Column(name = "left_at")
    private OffsetDateTime leftAt;

    protected ChatRoomMember() {
    }

    private ChatRoomMember(
            ChatRoom chatRoom,
            User user,
            ChatMemberRole role
    ) {
        this.chatRoom = chatRoom;
        this.user = user;
        this.role = role;
        this.notificationEnabled = true;
    }

    public static ChatRoomMember owner(
            ChatRoom chatRoom,
            User user
    ) {
        return new ChatRoomMember(chatRoom, user, ChatMemberRole.OWNER);
    }

    public static ChatRoomMember member(
            ChatRoom chatRoom,
            User user
    ) {
        return new ChatRoomMember(chatRoom, user, ChatMemberRole.MEMBER);
    }

    @PrePersist
    private void onCreate() {
        this.joinedAt = OffsetDateTime.now();
    }

    public void updateLastReadMessageId(
            Long lastReadMessageId
    ) {
        this.lastReadMessageId = lastReadMessageId;
    }

    public void changeNotificationEnabled(
            boolean notificationEnabled
    ) {
        this.notificationEnabled = notificationEnabled;
    }

    public void leave() {
        this.leftAt = OffsetDateTime.now();
    }

    public void rejoin() {
        this.leftAt = null;
    }

    public boolean isParticipating() {
        return this.leftAt == null;
    }

    public Long getId() {
        return id;
    }

    public ChatRoom getChatRoom() {
        return chatRoom;
    }

    public User getUser() {
        return user;
    }

    public ChatMemberRole getRole() {
        return role;
    }

    public Long getLastReadMessageId() {
        return lastReadMessageId;
    }

    public boolean isNotificationEnabled() {
        return notificationEnabled;
    }

    public OffsetDateTime getJoinedAt() {
        return joinedAt;
    }

    public OffsetDateTime getLeftAt() {
        return leftAt;
    }
}
