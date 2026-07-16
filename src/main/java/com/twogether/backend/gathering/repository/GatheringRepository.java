package com.twogether.backend.gathering.repository;

import com.twogether.backend.gathering.domain.Gathering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GatheringRepository
        extends JpaRepository<Gathering, Long> {

    // 상세 조회: host 를 함께 로딩해 N+1 회피
    @Query("select g from Gathering g join fetch g.host where g.id = :id")
    Optional<Gathering> findDetailById(
            @Param("id") Long id
    );
}
