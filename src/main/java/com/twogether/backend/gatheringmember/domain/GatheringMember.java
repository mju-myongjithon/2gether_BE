package com.twogether.backend.gatheringmember.domain;

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
@Table(name = "gathering_member")
public class GatheringMember {

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

    @Enumerated(EnumType.STRING)
    @Column(
            name = "role",
            nullable = false,
            length = 20
    )
    private GatheringMemberRole role;

    @Column(
            name = "joined_at",
            nullable = false
    )
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    protected GatheringMember() {
    }

    public GatheringMember(
            Long gatheringId,
            Long userId,
            GatheringMemberRole role
    ) {
        this.gatheringId = gatheringId;
        this.userId = userId;
        this.role = role;
        this.joinedAt = LocalDateTime.now();
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

    public GatheringMemberRole getRole() {
        return role;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public LocalDateTime getLeftAt() {
        return leftAt;
    }

    public boolean isActive() {
        return this.leftAt == null;
    }

    public boolean isHost() {
        return this.role == GatheringMemberRole.HOST;
    }

    public void leave() {
        this.leftAt = LocalDateTime.now();
    }
}
