package com.twogether.backend.gatheringcomment.repository;

import com.twogether.backend.gatheringcomment.domain.GatheringComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GatheringCommentRepository extends JpaRepository<GatheringComment, Long> {

    @Query("SELECT gc FROM GatheringComment gc JOIN FETCH gc.user WHERE gc.gathering.id = :gatheringId ORDER BY gc.createdAt ASC")
    List<GatheringComment> findAllByGatheringIdWithUser(@Param("gatheringId") Long gatheringId);
}
