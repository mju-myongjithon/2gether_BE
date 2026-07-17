package com.twogether.backend.bookmark.domain;

import com.twogether.backend.gathering.domain.Gathering;
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
        name = "gathering_bookmark",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_gathering_bookmark",
                        columnNames = {"bookmarker_id", "gathering_id"}
                )
        },
        indexes = {
                @Index(name = "idx_gathering_bookmark_bookmarker_id", columnList = "bookmarker_id"),
                @Index(name = "idx_gathering_bookmark_gathering_id", columnList = "gathering_id")
        }
)
public class GatheringBookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bookmarker_id", nullable = false)
    private User bookmarker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gathering_id", nullable = false)
    private Gathering gathering;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected GatheringBookmark() {
    }

    public GatheringBookmark(User bookmarker, Gathering gathering) {
        this.bookmarker = bookmarker;
        this.gathering = gathering;
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

    public Gathering getGathering() {
        return gathering;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}