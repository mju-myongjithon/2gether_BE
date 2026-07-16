package com.twogether.backend.gathering.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "gathering_tag",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_gathering_tag",
                        columnNames = {"gathering_id", "tag_id"}
                )
        },
        indexes = {
                @Index(name = "idx_gathering_tag_tag_id", columnList = "tag_id, gathering_id")
        }
)
public class GatheringTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "gathering_id",
            nullable = false
    )
    private Gathering gathering;

    // tags 도메인은 읽기 전용 참조 → 단순 FK(Long)로만 보관 (User.departmentId 패턴과 동일)
    @Column(
            name = "tag_id",
            nullable = false
    )
    private Long tagId;

    protected GatheringTag() {
    }

    public GatheringTag(
            Gathering gathering,
            Long tagId
    ) {
        this.gathering = gathering;
        this.tagId = tagId;
    }

    public Long getId() {
        return id;
    }

    public Gathering getGathering() {
        return gathering;
    }

    public Long getTagId() {
        return tagId;
    }
}
