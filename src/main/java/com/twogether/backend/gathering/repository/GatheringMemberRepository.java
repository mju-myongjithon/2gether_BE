package com.twogether.backend.gathering.repository;

import com.twogether.backend.gathering.domain.GatheringMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GatheringMemberRepository
        extends JpaRepository<GatheringMember, Long> {

        interface GatheringMemberCount {
                Long getGatheringId();

                Long getMemberCount();
        }

    boolean existsByGatheringIdAndUserId(
            Long gatheringId,
            Long userId
    );

    Optional<GatheringMember> findByGatheringIdAndUserId(
            Long gatheringId,
            Long userId
    );

    @Query("""
            select m from GatheringMember m
            join fetch m.gathering g
            join fetch g.host
            where m.user.id = :userId
            order by m.role asc, m.joinedAt desc
            """)
    List<GatheringMember> findByUserIdWithGathering(
            @Param("userId") Long userId
    );

    void deleteByGatheringIdAndUserId(
            Long gatheringId,
            Long userId
    );

    @Query("""
            select m.gathering.id as gatheringId, count(m) as memberCount
            from GatheringMember m
            where m.gathering.id in :gatheringIds
            group by m.gathering.id
            """)
    List<GatheringMemberCount> countMembersByGatheringIds(
            @Param("gatheringIds") List<Long> gatheringIds
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
}
