package com.twogether.backend.gathering.repository;

import com.twogether.backend.gathering.domain.GatheringTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatheringTagRepository
        extends JpaRepository<GatheringTag, Long> {

    void deleteAllByGatheringId(
            Long gatheringId
    );
}
