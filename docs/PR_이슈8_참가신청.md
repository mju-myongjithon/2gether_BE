## 개요
> 이 PR에서 변경한 내용을 간략하게 설명해주세요.

`POST /api/gatherings/{id}/applications` 모임 참가 신청 API를 더미 응답에서 실제 DB 저장으로 교체했습니다.
`GatheringApplication` 엔티티/리포지토리/서비스를 신규로 만들고, 모집중·멤버·중복 검증을 거쳐 PENDING 신청을 생성합니다.

## 재신청 정책 (결정 사항)

**REJECTED(거절) 후 재신청 허용** 으로 확정했습니다.
- 활성 신청(**PENDING/ACCEPTED**)이 있으면 재신청 불가(`DUPLICATE_APPLICATION`).
- 거절(REJECTED) 이력만 있으면 다시 신청 가능.

이 정책은 "활성 신청만 유일"한 **부분 유니크 인덱스**가 필요한데, JPA 애노테이션으로는 표현할 수 없습니다.
현재는 **서비스의 존재 여부 검사**로 막고 있으며, 동시 요청 레이스까지 DB 레벨로 막으려면 아래 인덱스를 **PostgreSQL에 수동 생성**해야 합니다(운영 반영 시 권장, ddl-auto 로는 생성되지 않음):

```sql
CREATE UNIQUE INDEX IF NOT EXISTS uq_gathering_application_active
ON gathering_application (gathering_id, user_id)
WHERE status IN ('PENDING', 'ACCEPTED');
```

> 장기적으로는 설계 문서 권고대로 Flyway 도입 후 이 DDL을 마이그레이션으로 관리하는 것을 권장합니다.

## 변경 사항
- `GatheringApplication` 엔티티 신규 — gathering/user `@ManyToOne(LAZY)`, status `@Enumerated(STRING)`, `applied_at` `@PrePersist`, 인덱스 `(user_id, status)`·`(gathering_id, status)`. (전체 UNIQUE 대신 부분 유니크 정책 → 유니크 제약 미부여)
- `GatheringApplicationRepository` 신규 — `existsByGatheringIdAndUserIdAndStatusIn`(활성 신청 중복 검사).
- `GatheringApplicationService.apply(...)` 신규 — 모집중 검증(`GATHERING_NOT_RECRUITING`) → 이미 멤버/방장 차단(`ALREADY_MEMBER`) → 활성 신청 중복 차단(`DUPLICATE_APPLICATION`) → PENDING 저장. 쓰기만 `@Transactional`.
- `GatheringApplicationCreateRequest` — `message` `@Size(max=300)` 검증 추가.
- `GatheringApplicationController.applyToGathering` — 더미 제거, `@AuthenticationPrincipal Jwt` + `@Valid` 연결, `201 Created` 반환.
- `ErrorCode` — `GATHERING_NOT_RECRUITING`, `DUPLICATE_APPLICATION`, `ALREADY_MEMBER` 추가(값 추가만).

**범위**: 이번 PR은 **신청 생성만** 구현합니다. 신청자 목록·수락·거절은 이슈9에서 이어집니다(해당 컨트롤러 메서드는 아직 더미).

## 관련 이슈
Closes #41

## 테스트
- [ ] 로컬 환경에서 테스트 완료
- [ ] 기존 기능 정상 동작 확인

**확인 시나리오(제안)**
- 모집중 모임에 일반 유저 신청 → 201, status=PENDING, appliedAt 기록.
- 방장이 자기 모임 신청 → 409 ALREADY_MEMBER.
- 이미 신청(PENDING)한 상태로 재신청 → 409 DUPLICATE_APPLICATION.
- 거절(REJECTED) 후 재신청 → 201(허용).
- CONFIRMED/CANCELED 모임 신청 → 409 GATHERING_NOT_RECRUITING.
- 없는 모임 → 404 GATHERING_NOT_FOUND. message 300자 초과 → 400.

## 체크리스트
- [ ] 코드 리뷰 요청 전 셀프 리뷰 완료
- [ ] 불필요한 코드/주석 제거
- [ ] 커밋 메시지 컨벤션 준수
