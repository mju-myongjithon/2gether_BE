package com.twogether.backend.gatheringnotice.repository;

import com.twogether.backend.gatheringnotice.domain.GatheringNotice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GatheringNoticeRepository extends JpaRepository<GatheringNotice, Long> {

    @Query("SELECT gn FROM GatheringNotice gn JOIN FETCH gn.gathering WHERE gn.gathering.id = :gatheringId ORDER BY gn.isPinned DESC, gn.createdAt DESC")
    Page<GatheringNotice> findAllByGatheringId(
            @Param("gatheringId") Long gatheringId,
            Pageable pageable
    );
}
