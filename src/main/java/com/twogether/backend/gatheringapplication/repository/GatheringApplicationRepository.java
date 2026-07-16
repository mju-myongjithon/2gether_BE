package com.twogether.backend.gatheringapplication.repository;

import com.twogether.backend.gatheringapplication.domain.ApplicationStatus;
import com.twogether.backend.gatheringapplication.domain.GatheringApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

public interface GatheringApplicationRepository
        extends JpaRepository<GatheringApplication, Long> {

    /**
     * 활성 신청(PENDING/ACCEPTED) 중복 여부 확인.
     * REJECTED 후 재신청을 허용하기 위해 상태를 함께 조건에 둔다.
     */
    boolean existsByGatheringIdAndUserIdAndStatusIn(
            Long gatheringId,
            Long userId,
            Collection<ApplicationStatus> statuses
    );

    // 수락/거절 처리: gathering·user 를 함께 로딩
    @Query("""
            select a from GatheringApplication a
            join fetch a.gathering
            join fetch a.user
            where a.id = :id
            """)
    Optional<GatheringApplication> findDetailById(
            @Param("id") Long id
    );

    // 방장의 신청함(전체): 신청자(user)를 함께 로딩(N+1 회피)
    @EntityGraph(attributePaths = "user")
    Page<GatheringApplication> findByGatheringId(
            Long gatheringId,
            Pageable pageable
    );

    // 방장의 신청함(상태 필터)
    @EntityGraph(attributePaths = "user")
    Page<GatheringApplication> findByGatheringIdAndStatus(
            Long gatheringId,
            ApplicationStatus status,
            Pageable pageable
    );
}
