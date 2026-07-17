package com.twogether.backend.bookmark.domain;

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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_bookmark",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_user_bookmark",
                        columnNames = {"bookmarker_id", "bookmarked_user_id"}
                )
        },
        indexes = {
                @Index(name = "idx_user_bookmark_bookmarker_id", columnList = "bookmarker_id"),
                @Index(name = "idx_user_bookmark_bookmarked_user_id", columnList = "bookmarked_user_id")
        }
)
public class UserBookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bookmarker_id", nullable = false)
    private User bookmarker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bookmarked_user_id", nullable = false)
    private User bookmarkedUser;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected UserBookmark() {
    }

    public UserBookmark(User bookmarker, User bookmarkedUser) {
        this.bookmarker = bookmarker;
        this.bookmarkedUser = bookmarkedUser;
    }

    @PrePersist
    private void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    private void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getBookmarker() {
        return bookmarker;
    }

    public User getBookmarkedUser() {
        return bookmarkedUser;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}