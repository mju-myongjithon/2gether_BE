package com.twogether.backend.gatheringinvitation.domain;

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

@Entity
@Table(
        name = "gathering_invitation",
        indexes = {
                @Index(name = "idx_gathering_invitation_invitee_status", columnList = "invitee_id, status"),
                @Index(name = "idx_gathering_invitation_gathering_status", columnList = "gathering_id, status"),
                @Index(name = "idx_gathering_invitation_inviter", columnList = "inviter_id")
        }
)
public class GatheringInvitation {

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
            name = "inviter_id",
            nullable = false
    )
    private User inviter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "invitee_id",
            nullable = false
    )
    private User invitee;

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
    private InvitationStatus status;

    @Column(
            name = "reason",
            length = 200
    )
    private String reason;

    @Column(
            name = "invited_at",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime invitedAt;

    @Column(name = "responded_at")
    private OffsetDateTime respondedAt;

    protected GatheringInvitation() {
    }

    private GatheringInvitation(
            Gathering gathering,
            User inviter,
            User invitee,
            String message
    ) {
        this.gathering = gathering;
        this.inviter = inviter;
        this.invitee = invitee;
        this.message = message;
        this.status = InvitationStatus.PENDING;
    }

    public static GatheringInvitation create(
            Gathering gathering,
            User inviter,
            User invitee,
            String message
    ) {
        return new GatheringInvitation(gathering, inviter, invitee, message);
    }

    @PrePersist
    private void onCreate() {
        this.invitedAt = OffsetDateTime.now();
        if (this.status == null) {
            this.status = InvitationStatus.PENDING;
        }
    }

    public boolean isPending() {
        return this.status == InvitationStatus.PENDING;
    }

    public void accept() {
        this.status = InvitationStatus.ACCEPTED;
        this.respondedAt = OffsetDateTime.now();
    }

    public void reject(String reason) {
        this.status = InvitationStatus.REJECTED;
        this.reason = reason;
        this.respondedAt = OffsetDateTime.now();
    }

    public void cancel() {
        this.status = InvitationStatus.CANCELLED;
        this.respondedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Gathering getGathering() {
        return gathering;
    }

    public User getInviter() {
        return inviter;
    }

    public User getInvitee() {
        return invitee;
    }

    public String getMessage() {
        return message;
    }

    public InvitationStatus getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public OffsetDateTime getInvitedAt() {
        return invitedAt;
    }

    public OffsetDateTime getRespondedAt() {
        return respondedAt;
    }
}
