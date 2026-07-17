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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 채팅 메시지.
 *
 * - id: 단조 증가(bigint) → 정렬/커서/읽음 비교의 기준.
 * - sender: SYSTEM 메시지는 NULL.
 * - type: TEXT / IMAGE / SYSTEM / CARD.
 * - content: 텍스트/시스템 안내문. 이미지는 message_attachment 로 분리.
 * - meta: JSONB. SYSTEM 상세({systemType, ...}) · CARD 카드데이터 등 스키마 변경 없이 흡수.
 * - clientMessageId: 소켓 재전송 멱등키. (chat_room_id, client_message_id) 유니크.
 * - deletedAt: 소프트 삭제.
 */
@Entity
@Table(
        name = "message",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_message_room_client_message_id",
                        columnNames = {"chat_room_id", "client_message_id"}
                )
        },
        indexes = {
                @Index(name = "idx_message_room_id_id", columnList = "chat_room_id, id")
        }
)
public class Message {

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
    @JoinColumn(name = "sender_id")
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "type",
            nullable = false,
            length = 20
    )
    private MessageType type;

    @Column(
            name = "content",
            columnDefinition = "text"
    )
    private String content;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
            name = "meta",
            columnDefinition = "jsonb"
    )
    private Map<String, Object> meta;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Column(name = "client_message_id")
    private UUID clientMessageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replied_to_message_id")
    private Message repliedToMessage;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;

    protected Message() {
    }

    private Message(
            ChatRoom chatRoom,
            User sender,
            MessageType type,
            String content,
            Map<String, Object> meta,
            UUID clientMessageId,
            Message repliedToMessage
    ) {
        this.chatRoom = chatRoom;
        this.sender = sender;
        this.type = type;
        this.content = content;
        this.meta = meta;
        this.clientMessageId = clientMessageId;
        this.repliedToMessage = repliedToMessage;
    }

    public static Message text(
            ChatRoom chatRoom,
            User sender,
            String content,
            UUID clientMessageId
    ) {
        return new Message(chatRoom, sender, MessageType.TEXT, content, null, clientMessageId, null);
    }

    public static Message text(
            ChatRoom chatRoom,
            User sender,
            String content,
            UUID clientMessageId,
            Message repliedToMessage
    ) {
        return new Message(chatRoom, sender, MessageType.TEXT, content, null, clientMessageId, repliedToMessage);
    }

    public static Message image(
            ChatRoom chatRoom,
            User sender,
            String content,
            UUID clientMessageId
    ) {
        return new Message(chatRoom, sender, MessageType.IMAGE, content, null, clientMessageId, null);
    }

    /**
     * 시스템 메시지(입장/퇴장/모임확정 등). sender=NULL, 상세는 meta 에 담는다.
     */
    public static Message system(
            ChatRoom chatRoom,
            String content,
            Map<String, Object> meta
    ) {
        return new Message(chatRoom, null, MessageType.SYSTEM, content, meta, null, null);
    }

    public static Message card(
            ChatRoom chatRoom,
            User sender,
            String content,
            Map<String, Object> meta
    ) {
        return new Message(chatRoom, sender, MessageType.CARD, content, meta, null, null);
    }

    @PrePersist
    private void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    public void softDelete() {
        this.deletedAt = OffsetDateTime.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public Long getId() {
        return id;
    }

    public ChatRoom getChatRoom() {
        return chatRoom;
    }

    public User getSender() {
        return sender;
    }

    public MessageType getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public Map<String, Object> getMeta() {
        return meta;
    }

    public OffsetDateTime getDeletedAt() {
        return deletedAt;
    }

    public UUID getClientMessageId() {
        return clientMessageId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public Message getRepliedToMessage() {
        return repliedToMessage;
    }

    public void setRepliedToMessage(Message repliedToMessage) {
        this.repliedToMessage = repliedToMessage;
    }
}
