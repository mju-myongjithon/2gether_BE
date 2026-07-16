package com.twogether.backend.gatheringapplication.domain;

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
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/**
 * 모임 참가 신청.
 *
 * 재신청 정책(REJECTED 후 재신청 허용): (gathering_id, user_id) 전체 UNIQUE 대신
 * 활성 신청(PENDING/ACCEPTED)만 유일해야 한다. 부분 유니크 인덱스는 JPA 애노테이션으로
 * 표현할 수 없어 DB에 수동으로 생성한다(README/PR의 SQL 참고). 애플리케이션 레벨에서는
 * 서비스가 존재 여부를 확인해 DUPLICATE_APPLICATION 으로 막는다.
 */
@Entity
@Table(
        name = "gathering_application",
        indexes = {
                @Index(name = "idx_gathering_application_user_status", columnList = "user_id, status"),
                @Index(name = "idx_gathering_application_gathering_status", columnList = "gathering_id, status")
        }
)
public class GatheringApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "gathering_id",
            nullable = false
    )
    private Gathering gathering;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

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
            nullable = false,
            updatable = false
    )
    private OffsetDateTime appliedAt;

    @Column(name = "reviewed_at")
    private OffsetDateTime reviewedAt;

    protected GatheringApplication() {
    }

    private GatheringApplication(
            Gathering gathering,
            User user,
            String message
    ) {
        this.gathering = gathering;
        this.user = user;
        this.message = message;
        this.status = ApplicationStatus.PENDING;
    }

    public static GatheringApplication create(
            Gathering gathering,
            User user,
            String message
    ) {
        return new GatheringApplication(gathering, user, message);
    }

    @PrePersist
    private void onCreate() {
        this.appliedAt = OffsetDateTime.now();
        if (this.status == null) {
            this.status = ApplicationStatus.PENDING;
        }
    }

    // --- 상태 전이 (방장 권한/정원 등 사전 검증은 서비스 계층에서 수행) ---

    public boolean isPending() {
        return this.status == ApplicationStatus.PENDING;
    }

    public void accept() {
        this.status = ApplicationStatus.ACCEPTED;
        this.reviewedAt = OffsetDateTime.now();
    }

    public void reject(
            String rejectReason
    ) {
        this.status = ApplicationStatus.REJECTED;
        this.rejectReason = rejectReason;
        this.reviewedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Gathering getGathering() {
        return gathering;
    }

    public User getUser() {
        return user;
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

    public OffsetDateTime getAppliedAt() {
        return appliedAt;
    }

    public OffsetDateTime getReviewedAt() {
        return reviewedAt;
    }
}
