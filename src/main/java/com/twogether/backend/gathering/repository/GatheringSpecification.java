package com.twogether.backend.gathering.repository;

import com.twogether.backend.gathering.domain.Gathering;
import com.twogether.backend.gathering.domain.GatheringCategory;
import com.twogether.backend.gathering.domain.GatheringStatus;
import org.springframework.data.jpa.domain.Specification;

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
