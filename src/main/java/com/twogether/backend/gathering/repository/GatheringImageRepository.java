package com.twogether.backend.gathering.repository;

import com.twogether.backend.gathering.domain.GatheringImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatheringImageRepository
        extends JpaRepository<GatheringImage, Long> {

    void deleteAllByGatheringId(
            Long gatheringId
    );
}
