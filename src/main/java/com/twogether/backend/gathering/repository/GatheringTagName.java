package com.twogether.backend.gathering.repository;

/**
 * 모임 목록의 태그 배치 조회 결과 프로젝션.
 * (gatheringId, tagName) 쌍으로 반환되어 서비스에서 모임별로 그룹핑된다.
 */
public interface GatheringTagName {

    Long getGatheringId();

    String getTagName();
}
