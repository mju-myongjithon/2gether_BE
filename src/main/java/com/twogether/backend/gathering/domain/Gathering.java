package com.twogether.backend.gathering.domain;

import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "gathering",
        indexes = {
                @Index(name = "idx_gathering_status_category", columnList = "status, category"),
                @Index(name = "idx_gathering_recruit_end_at", columnList = "recruit_end_at"),
                @Index(name = "idx_gathering_host_id", columnList = "host_id"),
                @Index(name = "idx_gathering_created_at", columnList = "created_at")
        }
)
public class Gathering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "host_id",
            nullable = false
    )
    private User host;

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

    @Enumerated(EnumType.STRING)
    @Column(
            name = "category",
            nullable = false,
            length = 20
    )
    private GatheringCategory category;

    @Column(
            name = "location",
            length = 150
    )
    private String location;

    @Column(
            name = "max_members",
            nullable = false
    )
    private short maxMembers;

    @Column(
            name = "current_members",
            nullable = false
    )
    private short currentMembers;

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
    private OffsetDateTime recruitStartAt;

    @Column(name = "recruit_end_at")
    private OffsetDateTime recruitEndAt;

    @Column(name = "meet_at")
    private OffsetDateTime meetAt;

    @Column(name = "confirmed_at")
    private OffsetDateTime confirmedAt;

    @Column(name = "canceled_at")
    private OffsetDateTime canceledAt;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private OffsetDateTime updatedAt;

    protected Gathering() {
    }

    public Gathering(
            User host,
            String title,
            String content,
            GatheringCategory category,
            String location,
            short maxMembers,
            boolean fusionEnabled,
            OffsetDateTime recruitStartAt,
            OffsetDateTime recruitEndAt,
            OffsetDateTime meetAt
    ) {
        validateMaxMembers(maxMembers);

        this.host = host;
        this.title = title;
        this.content = content;
        this.category = category;
        this.location = location;
        this.maxMembers = maxMembers;
        this.currentMembers = 1;
        this.fusionEnabled = fusionEnabled;
        this.status = GatheringStatus.RECRUITING;
        this.recruitStartAt = recruitStartAt;
        this.recruitEndAt = recruitEndAt;
        this.meetAt = meetAt;
    }

    private void validateMaxMembers(
            short maxMembers
    ) {
        if (maxMembers < 1) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    @PrePersist
    private void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    private void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    // --- 부분 수정 (권한/상태 검증은 서비스 계층에서 선수행) ---
    // 전달값은 서비스에서 "null이면 기존값 유지"로 해석해 최종값으로 넘긴다.

    public void update(
            String title,
            String content,
            GatheringCategory category,
            String location,
            short maxMembers,
            boolean fusionEnabled,
            OffsetDateTime meetAt
    ) {
        validateMaxMembers(maxMembers);

        this.title = title;
        this.content = content;
        this.category = category;
        this.location = location;
        this.maxMembers = maxMembers;
        this.fusionEnabled = fusionEnabled;
        this.meetAt = meetAt;
        this.updatedAt = OffsetDateTime.now();
    }

    // --- 상태 전이 (방장/모집중 등 사전 검증은 서비스 계층에서 수행) ---

    public void confirm() {
        this.status = GatheringStatus.CONFIRMED;
        this.confirmedAt = OffsetDateTime.now();
    }

    public void cancel() {
        this.status = GatheringStatus.CANCELED;
        this.canceledAt = OffsetDateTime.now();
    }

    public void increaseMember() {
        this.currentMembers++;
    }

    public boolean isRecruiting() {
        return status == GatheringStatus.RECRUITING;
    }

    public boolean isHost(
            Long userId
    ) {
        return host.getId().equals(userId);
    }

    public boolean isFull() {
        return currentMembers >= maxMembers;
    }

    // 파생 표시 상태 — 저장하지 않고 조회 시점에 계산
    @Transient
    public String displayStatus(
            OffsetDateTime now
    ) {
        if (status != GatheringStatus.RECRUITING) {
            return status.name();
        }
        if (recruitEndAt == null) {
            return "ALWAYS";
        }
        if (recruitStartAt != null && now.isBefore(recruitStartAt)) {
            return "UPCOMING";
        }
        if (!now.isAfter(recruitEndAt)) {
            return "RECRUITING";
        }
        return "CLOSED";
    }

    public Long getId() {
        return id;
    }

    public User getHost() {
        return host;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public GatheringCategory getCategory() {
        return category;
    }

    public String getLocation() {
        return location;
    }

    public short getMaxMembers() {
        return maxMembers;
    }

    public short getCurrentMembers() {
        return currentMembers;
    }

    public boolean isFusionEnabled() {
        return fusionEnabled;
    }

    public GatheringStatus getStatus() {
        return status;
    }

    public OffsetDateTime getRecruitStartAt() {
        return recruitStartAt;
    }

    public OffsetDateTime getRecruitEndAt() {
        return recruitEndAt;
    }

    public OffsetDateTime getMeetAt() {
        return meetAt;
    }

    public OffsetDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public OffsetDateTime getCanceledAt() {
        return canceledAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
