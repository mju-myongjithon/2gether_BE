package com.twogether.backend.gathering.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "gathering_image",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_gathering_image_sort_order",
                        columnNames = {"gathering_id", "sort_order"}
                )
        }
)
public class GatheringImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "gathering_id",
            nullable = false
    )
    private Gathering gathering;

    @Column(
            name = "image_url",
            nullable = false,
            length = 300
    )
    private String imageUrl;

    @Column(
            name = "sort_order",
            nullable = false
    )
    private short sortOrder;

    protected GatheringImage() {
    }

    public GatheringImage(
            Gathering gathering,
            String imageUrl,
            short sortOrder
    ) {
        this.gathering = gathering;
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
    }

    public Long getId() {
        return id;
    }

    public Gathering getGathering() {
        return gathering;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public short getSortOrder() {
        return sortOrder;
    }
}
