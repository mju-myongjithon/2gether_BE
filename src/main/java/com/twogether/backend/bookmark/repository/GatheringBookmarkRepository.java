package com.twogether.backend.bookmark.repository;

import com.twogether.backend.bookmark.domain.GatheringBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GatheringBookmarkRepository extends JpaRepository<GatheringBookmark, Long> {

    Optional<GatheringBookmark> findByBookmarkerIdAndGatheringId(Long bookmarkerId, Long gatheringId);

    boolean existsByBookmarkerIdAndGatheringId(Long bookmarkerId, Long gatheringId);

    @Query("""
            select gb from GatheringBookmark gb
            join fetch gb.gathering g
            join fetch g.host
            where gb.bookmarker.id = :bookmarkerId
            order by gb.createdAt desc
            """)
    List<GatheringBookmark> findAllByBookmarkerIdWithGathering(@Param("bookmarkerId") Long bookmarkerId);

    void deleteAllByBookmarkerIdOrGatheringId(Long bookmarkerId, Long gatheringId);
}