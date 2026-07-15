package com.twogether.backend.gatheringapplication.repository;

import com.twogether.backend.gatheringapplication.domain.GatheringApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GatheringApplicationRepository extends JpaRepository<GatheringApplication, Long> {

    Optional<GatheringApplication> findByGatheringIdAndUserId(Long gatheringId, Long userId);

    Page<GatheringApplication> findByGatheringIdOrderByAppliedAtDesc(Long gatheringId, Pageable pageable);

    Page<GatheringApplication> findByUserIdOrderByAppliedAtDesc(Long userId, Pageable pageable);

    boolean existsByGatheringIdAndUserId(Long gatheringId, Long userId);
}
