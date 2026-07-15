package com.twogether.backend.gathering.domain;

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
@Table(name = "gathering")
public class Gathering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "host_id",
            nullable = false
    )
    private Long hostId;

    @Column(
            name = "title",
            nullable = false,
            length = 120
    )
    private String title;

    @Column(
            name = "content",
            columnDefinition = "text"
    )
    private String content;

    @Column(
            name = "category",
            length = 30
    )
    private String category;

    @Column(
            name = "location",
            length = 150
    )
    private String location;

    @Column(
            name = "max_members",
            nullable = false
    )
    private Integer maxMembers;

    @Column(
            name = "fusion_enabled",
            nullable = false
    )
    private boolean fusionEnabled;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private GatheringStatus status;

    @Column(name = "recruit_start_at")
    private LocalDateTime recruitStartAt;

    @Column(name = "recruit_end_at")
    private LocalDateTime recruitEndAt;

    @Column(name = "meet_at")
    private LocalDateTime meetAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    protected Gathering() {
    }

    public Gathering(
            Long hostId,
            String title,
            String content,
            String category,
            String location,
            Integer maxMembers,
            boolean fusionEnabled,
            LocalDateTime recruitStartAt,
            LocalDateTime recruitEndAt,
            LocalDateTime meetAt
    ) {
        this.hostId = hostId;
        this.title = title;
        this.content = content;
        this.category = category;
        this.location = location;
        this.maxMembers = maxMembers;
        this.fusionEnabled = fusionEnabled;
        this.status = GatheringStatus.RECRUITING;
        this.recruitStartAt = recruitStartAt;
        this.recruitEndAt = recruitEndAt;
        this.meetAt = meetAt;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getHostId() {
        return hostId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getCategory() {
        return category;
    }

    public String getLocation() {
        return location;
    }

    public Integer getMaxMembers() {
        return maxMembers;
    }

    public boolean isFusionEnabled() {
        return fusionEnabled;
    }

    public GatheringStatus getStatus() {
        return status;
    }

    public LocalDateTime getRecruitStartAt() {
        return recruitStartAt;
    }

    public LocalDateTime getRecruitEndAt() {
        return recruitEndAt;
    }

    public LocalDateTime getMeetAt() {
        return meetAt;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public LocalDateTime getCanceledAt() {
        return canceledAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public boolean isHost(Long userId) {
        return this.hostId.equals(userId);
    }

    public boolean isRecruiting() {
        return this.status == GatheringStatus.RECRUITING;
    }

    public void update(
            String title,
            String content,
            String category,
            String location,
            Integer maxMembers,
            boolean fusionEnabled,
            LocalDateTime recruitStartAt,
            LocalDateTime recruitEndAt,
            LocalDateTime meetAt
    ) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.location = location;
        this.maxMembers = maxMembers;
        this.fusionEnabled = fusionEnabled;
        this.recruitStartAt = recruitStartAt;
        this.recruitEndAt = recruitEndAt;
        this.meetAt = meetAt;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = GatheringStatus.CANCELED;
        this.canceledAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void confirm() {
        this.status = GatheringStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
