package com.twogether.backend.gathering.repository;

import com.twogether.backend.gathering.domain.GatheringImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GatheringImageRepository
        extends JpaRepository<GatheringImage, Long> {

    @Modifying(flushAutomatically = true)
    @Query("""
            delete from GatheringImage gi
            where gi.gathering.id = :gatheringId
            """)
    int deleteAllByGatheringId(
            Long gatheringId
    );

    List<GatheringImage> findByGatheringIdOrderBySortOrderAsc(
            Long gatheringId
    );
}
