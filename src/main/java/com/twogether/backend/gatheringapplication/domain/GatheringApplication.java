package com.twogether.backend.gatheringapplication.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "gathering_application")
public class GatheringApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "gathering_id",
            nullable = false
    )
    private Long gatheringId;

    @Column(
            name = "user_id",
            nullable = false
    )
    private Long userId;

    @Column(
            name = "message",
            length = 300
    )
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private ApplicationStatus status;

    @Column(
            name = "reject_reason",
            length = 200
    )
    private String rejectReason;

    @Column(
            name = "applied_at",
            nullable = false
    )
    private LocalDateTime appliedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    protected GatheringApplication() {
    }

    public GatheringApplication(
            Long gatheringId,
            Long userId,
            String message
    ) {
        this.gatheringId = gatheringId;
        this.userId = userId;
        this.message = message;
        this.status = ApplicationStatus.PENDING;
        this.appliedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getGatheringId() {
        return gatheringId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getMessage() {
        return message;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public boolean isPending() {
        return this.status == ApplicationStatus.PENDING;
    }

    public void accept() {
        this.status = ApplicationStatus.ACCEPTED;
        this.reviewedAt = LocalDateTime.now();
    }

    public void reject(String rejectReason) {
        this.status = ApplicationStatus.REJECTED;
        this.rejectReason = rejectReason;
        this.reviewedAt = LocalDateTime.now();
    }
}
