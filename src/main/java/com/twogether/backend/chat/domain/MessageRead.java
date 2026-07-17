package com.twogether.backend.chat.domain;

import com.twogether.backend.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "message_read",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_message_read_message_user",
                        columnNames = {"message_id", "user_id"}
                )
        }
)
public class MessageRead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "read_at", nullable = false, updatable = false)
    private OffsetDateTime readAt;

    protected MessageRead() {
    }

    public MessageRead(Message message, User user) {
        this.message = message;
        this.user = user;
    }

    @PrePersist
    private void onCreate() {
        this.readAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Message getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }

    public OffsetDateTime getReadAt() {
        return readAt;
    }
}
