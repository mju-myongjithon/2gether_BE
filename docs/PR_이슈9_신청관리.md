## 개요
> 이 PR에서 변경한 내용을 간략하게 설명해주세요.

모임 신청 관리 API 3종을 더미에서 실제 구현으로 교체했습니다.
- `GET /api/gatherings/{id}/applications` — 방장의 신청함 조회(상태 필터·페이징)
- `POST /api/gathering-applications/{appId}/accept` — 수락(멤버 생성 + 정원 증가, 원자적)
- `POST /api/gathering-applications/{appId}/reject` — 거절(사유 저장)

## 핵심: 수락의 원자성 · 정원 동시성

수락은 **status=ACCEPTED + gathering_member 생성 + current_members 증가**를 한 트랜잭션에서 처리합니다.
동시에 여러 방장이(또는 중복 요청으로) 수락해 정원을 넘기는 것을 막기 위해, 모임 행에 **비관적 쓰기 락**(`SELECT ... FOR UPDATE`)을 건 뒤 정원(`current_members < max_members`)을 검사하고 증가시킵니다.

- `GatheringRepository.findByIdForUpdate` — `@Lock(PESSIMISTIC_WRITE)` 로 모임 행 잠금.
- 정원 초과 시 409 `CAPACITY_EXCEEDED`.

## 변경 사항
- `ErrorCode` — `APPLICATION_NOT_FOUND(404)`, `APPLICATION_ALREADY_PROCESSED(409)`, `CAPACITY_EXCEEDED(409)` 추가(값 추가만).
- `GatheringApplication` — 도메인 메서드 `isPending()`, `accept()`, `reject(reason)` 추가(`reviewed_at` 기록).
- `GatheringRepository.findByIdForUpdate` — 정원 동시성 제어용 비관적 락 조회 추가.
- `GatheringApplicationRepository` — `findDetailById`(gathering·user fetch join), `findByGatheringId`/`findByGatheringIdAndStatus`(user EntityGraph, 페이징) 추가.
- `GatheringApplicationService` — `getApplications`(방장 검증·상태 필터·학과명 배치·신청 최신순), `accept`(락·정원·원자적 처리), `reject`(사유 저장) 추가.
- `GatheringApplicationController` — 목록/수락/거절 3개 엔드포인트를 서비스에 연결, `@AuthenticationPrincipal Jwt` 주입.

## 알려진 제약 (후속 이슈)
- **신청자 취미/기술 태그**: `ApplicantResponse.hobbyTags/skillTags` 는 `user_tag`(사용자-태그 연결) 도메인이 아직 없어 **현재 빈 목록**으로 반환합니다. 해당 도메인 구축 후 채웁니다. `campus` 는 캠퍼스 비율 기능 제외로 null.
- **"내 신청 목록"**(`GET /api/users/me/applications`)은 이번 이슈 범위 밖이라 더미로 남겨뒀습니다(별도 이슈 권장).
- 상태 전이 잘못(이미 처리된 신청) 시 `APPLICATION_ALREADY_PROCESSED`, 없는 신청 `APPLICATION_NOT_FOUND` 는 명세의 대표 예외 목록엔 없지만 정확한 동작을 위해 추가했습니다.

## 관련 이슈
Closes #42

## 테스트
- [ ] 로컬 환경에서 테스트 완료
- [ ] 기존 기능 정상 동작 확인

**확인 시나리오(제안)**
- 방장이 신청함 조회(status=PENDING 필터/전체) → 최신순 페이징.
- 방장 아닌 사용자가 신청함 조회 → 403 FORBIDDEN.
- 수락 → 200, status=ACCEPTED, gathering_member 생성, current_members +1, memberId 반환.
- 정원 가득 찬 상태에서 수락 → 409 CAPACITY_EXCEEDED.
- 이미 수락/거절된 신청 재처리 → 409 APPLICATION_ALREADY_PROCESSED.
- 거절 → 200, status=REJECTED, rejectReason 저장. 이후 해당 유저 재신청 가능(이슈8 정책).
- 없는 신청 ID → 404 APPLICATION_NOT_FOUND.

## 체크리스트
- [ ] 코드 리뷰 요청 전 셀프 리뷰 완료
- [ ] 불필요한 코드/주석 제거
- [ ] 커밋 메시지 컨벤션 준수
