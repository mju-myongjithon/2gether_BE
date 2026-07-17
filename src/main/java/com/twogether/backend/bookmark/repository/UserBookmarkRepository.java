package com.twogether.backend.bookmark.repository;

import com.twogether.backend.bookmark.domain.UserBookmark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserBookmarkRepository extends JpaRepository<UserBookmark, Long> {

    Optional<UserBookmark> findByBookmarkerIdAndBookmarkedUserId(Long bookmarkerId, Long bookmarkedUserId);

    boolean existsByBookmarkerIdAndBookmarkedUserId(Long bookmarkerId, Long bookmarkedUserId);

    List<UserBookmark> findByBookmarkerIdOrderByCreatedAtDesc(Long bookmarkerId);

    void deleteAllByBookmarkerIdOrBookmarkedUserId(Long bookmarkerId, Long bookmarkedUserId);
}