## 개요
> 이 PR에서 변경한 내용을 간략하게 설명해주세요.

`GET /api/gatherings/{id}` 모임 상세 조회 API를 더미 응답에서 실제 DB 조회로 교체했습니다.
모임 정보 + 멤버 목록 + 태그 + 이미지 + 로그인 사용자의 참여 상태를 조립해 반환하며, 존재하지 않으면 404를 던집니다.

- host 는 fetch join, 멤버는 user fetch join 으로 N+1 방지.
- 멤버·방장 학과명은 한 번의 배치 IN 조회로 매핑, 태그는 이슈3의 배치 쿼리 재사용.
- 캠퍼스 비율 기능 제외에 따라 `campusRatio` 필드와 `CampusRatioResponse` 를 삭제, campus 는 null.

## 변경 사항
- `ErrorCode` — `GATHERING_NOT_FOUND(404)` 추가(값 추가만).
- `GatheringDetailResponse` — `campusRatio` 제거, `tags(List<String>)`·`images(List<String>)`·`displayStatus(String)` 추가, `of(...)` 팩토리 도입. category 는 영문 enum 값, 방장 campus 는 null.
- `CampusRatioResponse.java` **삭제**(더 이상 참조 없음).
- `GatheringMemberRepository.findByGatheringIdWithUser` 추가 — 멤버를 user 와 함께 로딩, HOST 우선·참여순 정렬.
- `GatheringImageRepository.findByGatheringIdOrderBySortOrderAsc` 추가.
- `GatheringService.getGatheringDetail(gatheringId, authUserId)` 추가 — 상세 조립 + isHost/isMember 계산. 조회는 `@Transactional(readOnly=true)`(클래스 기본).
- `GatheringController.getGathering` — 더미 제거, 서비스 연결. `@AuthenticationPrincipal Jwt` 를 optional 로 받아 **비로그인 조회 허용**(내 상태 필드 null).

**결정 사항 반영**
- `myApplicationStatus` : 신청(application) 도메인이 아직 없어(이슈8) **현재 항상 null**. 응답 필드는 유지하고, 이슈8에서 실제 조회로 대체 예정(서비스/컨트롤러/DTO에 TODO 주석 명시).
- 비로그인 상세 조회 **허용**(팀 정책: 허용 시 내 상태 필드 null).

## 관련 이슈
> 관련 이슈가 있다면 연결해주세요.

Closes #(이슈4: 모임 상세 조회 API 번호 입력)

## 테스트
- [ ] 로컬 환경에서 테스트 완료
- [ ] 기존 기능 정상 동작 확인

**확인 시나리오(제안)**
- 존재하는 모임 → 상세·멤버·태그·이미지 정상 반환, 멤버는 HOST 먼저.
- 없는 ID → 404 `GATHERING_NOT_FOUND`.
- 비로그인(토큰 없이) 조회 → 200, isHost/isMember=false, myApplicationStatus=null.
- 방장 토큰으로 조회 → isHost=true. 멤버 토큰 → isMember=true.
- 멤버/방장 조회 시 user·department 쿼리가 배치로만 나가는지(N+1 없음) 로그 확인.

## 체크리스트
- [ ] 코드 리뷰 요청 전 셀프 리뷰 완료
- [ ] 불필요한 코드/주석 제거
- [ ] 커밋 메시지 컨벤션 준수
