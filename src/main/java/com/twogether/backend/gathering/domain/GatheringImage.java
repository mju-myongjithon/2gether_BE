package com.twogether.backend.gathering.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "gathering_image")
public class GatheringImage {

    public static final int MAX_IMAGE_COUNT = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "gathering_id",
            nullable = false
    )
    private Long gatheringId;

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
    private Integer sortOrder;

    protected GatheringImage() {
    }

    public GatheringImage(
            Long gatheringId,
            String imageUrl,
            Integer sortOrder
    ) {
        this.gatheringId = gatheringId;
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
    }

    public Long getId() {
        return id;
    }

    public Long getGatheringId() {
        return gatheringId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }
}
