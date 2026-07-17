package com.twogether.backend.gathering.repository;

import com.twogether.backend.gathering.domain.Gathering;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.time.OffsetDateTime;
import java.util.Collection;
import com.twogether.backend.gathering.domain.GatheringStatus;

public interface GatheringRepository
        extends JpaRepository<Gathering, Long>,
        JpaSpecificationExecutor<Gathering> {

    // 상세 조회: host 를 함께 로딩해 N+1 회피
    @Query("select g from Gathering g join fetch g.host where g.id = :id")
    Optional<Gathering> findDetailById(
            @Param("id") Long id
    );

    // 신청 수락 등 정원 증감 시 동시성 제어용 비관적 쓰기 락
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from Gathering g where g.id = :id")
    Optional<Gathering> findByIdForUpdate(
            @Param("id") Long id
    );

    // 목록 조회: 동적 조건(Specification) + host 를 EntityGraph 로 함께 로딩(N+1 회피)
    @Override
    @EntityGraph(attributePaths = "host")
    Page<Gathering> findAll(
            Specification<Gathering> spec,
            Pageable pageable
    );

    long countByConfirmedAtGreaterThanEqualAndConfirmedAtLessThanAndStatusIn(
            OffsetDateTime start, OffsetDateTime end, Collection<GatheringStatus> statuses
    );
}
