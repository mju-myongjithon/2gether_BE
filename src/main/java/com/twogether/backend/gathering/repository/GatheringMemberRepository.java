package com.twogether.backend.gathering.repository;

import com.twogether.backend.gathering.domain.GatheringMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatheringMemberRepository
        extends JpaRepository<GatheringMember, Long> {

    boolean existsByGatheringIdAndUserId(
            Long gatheringId,
            Long userId
    );
}
