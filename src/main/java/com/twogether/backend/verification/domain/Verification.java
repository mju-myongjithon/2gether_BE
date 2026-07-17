package com.twogether.backend.verification.domain;

import com.twogether.backend.gathering.domain.Gathering;
import com.twogether.backend.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
        name = "verification",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_verification_gathering_id",
                        columnNames = "gathering_id"
                )
        }
)
public class Verification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gathering_id", nullable = false)
    private Gathering gathering;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id", nullable = false)
    private User uploader;

    @Column(name = "photo_url", nullable = false, length = 300)
    private String photoUrl;

    @Column(name = "review_text", length = 200)
    private String reviewText;

    @Enumerated(EnumType.STRING)
    @Column(name = "ai_status", nullable = false, length = 20)
    private AiStatus aiStatus;

    @Column(name = "ai_reason", length = 300)
    private String aiReason;

    @Column(name = "rejection_reason", length = 200)
    private String rejectionReason;

    @Column(name = "canvas_pixel_x")
    private Integer canvasPixelX;

    @Column(name = "canvas_pixel_y")
    private Integer canvasPixelY;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "verified_at")
    private OffsetDateTime verifiedAt;

    protected Verification() {
    }

    public Verification(
            Gathering gathering,
            User uploader,
            String photoUrl,
            String reviewText
    ) {
        this.gathering = gathering;
        this.uploader = uploader;
        this.photoUrl = photoUrl;
        this.reviewText = reviewText;
        this.aiStatus = AiStatus.PENDING;
    }

    @PrePersist
    private void onCreate() {
        this.createdAt = OffsetDateTime.now();
        if (this.aiStatus == null) {
            this.aiStatus = AiStatus.PENDING;
        }
    }

    public Long getId() { return id; }
    public Gathering getGathering() { return gathering; }
    public User getUploader() { return uploader; }
    public String getPhotoUrl() { return photoUrl; }
    public String getReviewText() { return reviewText; }
    public AiStatus getAiStatus() { return aiStatus; }
    public String getAiReason() { return aiReason; }
    public String getRejectionReason() { return rejectionReason; }
    public Integer getCanvasPixelX() { return canvasPixelX; }
    public Integer getCanvasPixelY() { return canvasPixelY; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getVerifiedAt() { return verifiedAt; }
}
