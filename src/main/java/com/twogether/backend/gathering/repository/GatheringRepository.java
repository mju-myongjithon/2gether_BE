package com.twogether.backend.gathering.repository;

import com.twogether.backend.gathering.domain.Gathering;
import com.twogether.backend.gathering.domain.GatheringStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GatheringRepository extends JpaRepository<Gathering, Long> {

    @Query("""
            SELECT g FROM Gathering g
            WHERE (:category IS NULL OR g.category = :category)
              AND (:status IS NULL OR g.status = :status)
              AND (:keyword IS NULL
                    OR g.title LIKE CONCAT('%', :keyword, '%')
                    OR g.content LIKE CONCAT('%', :keyword, '%'))
            ORDER BY g.createdAt DESC
            """)
    Page<Gathering> search(
            @Param("category") String category,
            @Param("status") GatheringStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
