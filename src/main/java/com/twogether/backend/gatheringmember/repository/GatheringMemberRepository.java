package com.twogether.backend.gatheringmember.repository;

import com.twogether.backend.gatheringmember.domain.GatheringMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GatheringMemberRepository extends JpaRepository<GatheringMember, Long> {

    Optional<GatheringMember> findByGatheringIdAndUserIdAndLeftAtIsNull(Long gatheringId, Long userId);

    List<GatheringMember> findByGatheringIdAndLeftAtIsNullOrderByJoinedAtAsc(Long gatheringId);

    Page<GatheringMember> findByGatheringIdAndLeftAtIsNullOrderByJoinedAtAsc(Long gatheringId, Pageable pageable);

    long countByGatheringIdAndLeftAtIsNull(Long gatheringId);

    boolean existsByGatheringIdAndUserIdAndLeftAtIsNull(Long gatheringId, Long userId);
}
