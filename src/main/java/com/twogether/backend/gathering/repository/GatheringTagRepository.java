package com.twogether.backend.gathering.repository;

import com.twogether.backend.gathering.domain.GatheringTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GatheringTagRepository
        extends JpaRepository<GatheringTag, Long> {

    void deleteAllByGatheringId(
            Long gatheringId
    );

    /**
     * 여러 모임의 태그명을 한 번의 IN 쿼리로 배치 조회한다(목록 N+1 회피).
     * tags 도메인은 읽기 전용 참조이므로 GatheringTag.tagId ↔ Tag.id 로 조인한다.
     */
    @Query("""
            select gt.gathering.id as gatheringId, t.name as tagName
            from GatheringTag gt, com.twogether.backend.tag.domain.Tag t
            where gt.tagId = t.id
              and gt.gathering.id in :gatheringIds
            """)
    List<GatheringTagName> findTagNamesByGatheringIds(
            @Param("gatheringIds") List<Long> gatheringIds
    );
}
