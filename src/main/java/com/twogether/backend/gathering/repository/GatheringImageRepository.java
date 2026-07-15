package com.twogether.backend.gathering.repository;

import com.twogether.backend.gathering.domain.GatheringImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GatheringImageRepository extends JpaRepository<GatheringImage, Long> {

    List<GatheringImage> findByGatheringIdOrderBySortOrderAsc(Long gatheringId);

    void deleteByGatheringId(Long gatheringId);
}
