package com.twogether.backend.gathering.domain;

import com.twogether.backend.gatheringmember.domain.GatheringMemberRole;
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
import jakarta.persistence.UniqueConstraint;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "gathering_member",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_gathering_member",
                        columnNames = {"gathering_id", "user_id"}
                )
        },
        indexes = {
                @Index(name = "idx_gathering_member_user_id", columnList = "user_id")
        }
)
public class GatheringMember {

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

    @Enumerated(EnumType.STRING)
    @Column(
            name = "role",
            nullable = false,
            length = 20
    )
    private GatheringMemberRole role;

    @Column(
            name = "joined_at",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime joinedAt;

    protected GatheringMember() {
    }

    private GatheringMember(
            Gathering gathering,
            User user,
            GatheringMemberRole role
    ) {
        this.gathering = gathering;
        this.user = user;
        this.role = role;
    }

    public static GatheringMember host(
            Gathering gathering,
            User user
    ) {
        return new GatheringMember(
                gathering,
                user,
                GatheringMemberRole.HOST
        );
    }

    public static GatheringMember member(
            Gathering gathering,
            User user
    ) {
        return new GatheringMember(
                gathering,
                user,
                GatheringMemberRole.MEMBER
        );
    }

    @PrePersist
    private void onCreate() {
        this.joinedAt = OffsetDateTime.now();
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

    public GatheringMemberRole getRole() {
        return role;
    }

    public OffsetDateTime getJoinedAt() {
        return joinedAt;
    }
}
