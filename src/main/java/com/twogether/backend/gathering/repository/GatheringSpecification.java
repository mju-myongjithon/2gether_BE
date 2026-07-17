package com.twogether.backend.gathering.repository;

import com.twogether.backend.gathering.domain.Gathering;
import com.twogether.backend.gathering.domain.GatheringCategory;
import com.twogether.backend.gathering.domain.GatheringStatus;
import com.twogether.backend.gathering.domain.GatheringTag;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * 모임 목록 조회의 동적 조건 조각 모음.
 *
 * 각 메서드는 조건 값이 없으면 {@code null} 을 반환하며,
 * {@link Specification#allOf} 로 조립할 때 null 조각은 자동으로 무시된다.
 */
public final class GatheringSpecification {

    private GatheringSpecification() {
    }

    public static Specification<Gathering> categoryEquals(
            GatheringCategory category
    ) {
        if (category == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("category"), category);
    }

    public static Specification<Gathering> statusEquals(
            GatheringStatus status
    ) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    /**
     * 제목 또는 내용에 키워드가 포함되면 매칭(대소문자 무시).
     *
     * LIKE '%..%' 는 인덱스를 타지 못하므로, 데이터가 커지면
     * pg_trgm GIN 인덱스 도입을 별도 이슈로 검토한다(설계 문서 5장).
     */
    /**
     * 선택한 태그 중 하나라도 가진 모임이면 매칭(OR).
     *
     * gathering_tag 에 대한 exists 서브쿼리로 판정하며,
     * (tag_id, gathering_id) 인덱스(idx_gathering_tag_tag_id)를 활용한다.
     * 존재하지 않는 tagId 는 어떤 모임과도 매칭되지 않으므로 자연스럽게 무시된다.
     */
    public static Specification<Gathering> hasAnyTag(
            List<Long> tagIds
    ) {
        if (tagIds == null || tagIds.isEmpty()) {
            return null;
        }
        return (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<GatheringTag> gatheringTag = subquery.from(GatheringTag.class);
            subquery.select(gatheringTag.get("gathering").get("id"))
                    .where(
                            cb.equal(gatheringTag.get("gathering").get("id"), root.get("id")),
                            gatheringTag.get("tagId").in(tagIds)
                    );
            return cb.exists(subquery);
        };
    }

    public static Specification<Gathering> keywordContains(
            String keyword
    ) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        String pattern = "%" + keyword.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), pattern),
                cb.like(cb.lower(root.get("content")), pattern)
        );
    }
}
