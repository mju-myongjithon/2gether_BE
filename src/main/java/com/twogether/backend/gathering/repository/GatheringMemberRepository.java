package com.twogether.backend.gathering.repository;

import com.twogether.backend.gathering.domain.GatheringMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GatheringMemberRepository
        extends JpaRepository<GatheringMember, Long> {

    boolean existsByGatheringIdAndUserId(
            Long gatheringId,
            Long userId
    );

    // 멤버 목록(페이징): 사용자(user)를 함께 로딩(N+1 회피). 정렬은 Pageable 로 주입.
    @EntityGraph(attributePaths = "user")
    Page<GatheringMember> findByGatheringId(
            Long gatheringId,
            Pageable pageable
    );

    /**
     * 특정 모임의 멤버 목록을 user 와 함께 로딩(N+1 회피).
     * role STRING 기준 정렬로 HOST 가 MEMBER 보다 먼저 오고, 같은 역할은 참여 순.
     */
    @Query("""
            select m from GatheringMember m
            join fetch m.user u
            where m.gathering.id = :gatheringId
            order by m.role asc, m.joinedAt asc
            """)
    List<GatheringMember> findByGatheringIdWithUser(
            @Param("gatheringId") Long gatheringId
    );

    @Query("select count(distinct m.user.id) from GatheringMember m where m.gathering.id in :gatheringIds")
    long countDistinctUsersByGatheringIds(@Param("gatheringIds") List<Long> gatheringIds);
}
