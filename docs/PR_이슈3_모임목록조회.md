## 개요
> 이 PR에서 변경한 내용을 간략하게 설명해주세요.

`GET /api/gatherings` 모임 목록 조회 API를 더미 응답에서 실제 DB 조회로 교체했습니다.
카테고리·상태·검색어 동적 조건과 페이징을 지원하며, 목록 카드에 필요한 방장·현재인원·태그를 N+1 없이 채웁니다.

- 동적 조건은 **JPA Specification**으로 구현(QueryDSL 미도입, `build.gradle` 변경 없음).
- 방장(host)은 `@EntityGraph`로 함께 로딩, 태그명·학과명은 각각 **배치 IN 쿼리 1회**로 조회.
- 현재 인원은 `current_members` 컬럼 사용, 정렬은 `created_at DESC` 고정.
- 응답에 태그 목록(`tags`)과 화면 표시 상태(`displayStatus`)를 포함.

## 변경 사항
- `GatheringSpecification` 추가 — category/status/keyword(title·content, 대소문자 무시) 동적 조건 조각. 값이 없으면 null 반환하여 `Specification.allOf`에서 자동 제외.
- `GatheringTagName` 프로젝션 + `GatheringTagRepository.findTagNamesByGatheringIds` 추가 — 여러 모임의 태그명을 한 번의 IN 쿼리로 조회.
- `GatheringRepository` — `JpaSpecificationExecutor<Gathering>` 상속, `findAll(Specification, Pageable)`을 `@EntityGraph(attributePaths="host")`로 오버라이드해 host N+1 방지.
- `GatheringSummaryResponse` — `tags(List<String>)`, `displayStatus(String)` 필드 추가 및 `of(...)` 팩토리 도입. category는 영문 enum 값으로 반환, 방장 campus는 null(캠퍼스 비율 기능 제외).
- `GatheringService.getGatherings(...)` 추가 — Specification 조립 → 페이징 조회 → 태그·학과 배치 매핑 → `PageResponse` 변환. category/status 문자열은 서비스에서 enum 파싱(실패 시 400 INVALID_REQUEST). 조회는 `@Transactional(readOnly=true)`(클래스 기본).
- `GatheringController.getGatherings` — 더미 제거, 서비스 연결. category/status 쿼리 파라미터를 `String`으로 받아 잘못된 값에서도 400으로 안전 처리.

**참고**
- `DepartmentRepository`는 조회 목적(read)으로만 사용, department 도메인 코드는 수정하지 않음.
- `ErrorCode` 추가 없음(기존 `INVALID_REQUEST` 재사용).
- keyword는 `LIKE '%..%'`라 데이터 증가 시 인덱스 미탐 → 향후 `pg_trgm` GIN 인덱스 도입은 별도 이슈로 검토.

## 관련 이슈
> 관련 이슈가 있다면 연결해주세요.

Closes #(이슈3: 모임 목록 조회 API 번호 입력)

## 테스트
- [ ] 로컬 환경에서 테스트 완료
- [ ] 기존 기능 정상 동작 확인

**확인 시나리오(제안)**
- 파라미터 없음 → 전체 모임 최신순(page=0, size=20) 반환.
- `category=HACKATHON` / `status=RECRUITING` / `keyword=해커톤` 각각·조합 필터 동작.
- 잘못된 `category`/`status` 값 → 400 `INVALID_REQUEST`.
- 목록 항목의 `host`, `currentMemberCount`, `tags`, `displayStatus` 정상 채워짐.
- 태그 없는 모임 → `tags: []`, 온보딩 이전 방장 → `host.departmentName: null`.
- 조회 시 host/태그/학과 쿼리가 각각 1회씩만 나가는지(N+1 없음) 로그 확인.

## 체크리스트
- [ ] 코드 리뷰 요청 전 셀프 리뷰 완료
- [ ] 불필요한 코드/주석 제거
- [ ] 커밋 메시지 컨벤션 준수
