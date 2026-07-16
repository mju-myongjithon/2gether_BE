## 개요
> 이 PR에서 변경한 내용을 간략하게 설명해주세요.

`POST /api/gatherings/{id}/cancel` 모임 취소 API를 더미 응답에서 실제 DB 반영으로 교체했습니다.
방장이 모집 중(RECRUITING)인 모임을 취소하며, 물리 삭제가 아니라 `status = CANCELED` + `canceled_at` 기록(소프트 취소)입니다.

## 변경 사항
- `GatheringService.cancel(authUserId, gatheringId)` 추가 — 방장 검증(`FORBIDDEN`) + 모집중 검증(`GATHERING_NOT_MODIFIABLE`) 후 `gathering.cancel()` 호출. 없는 모임이면 `GATHERING_NOT_FOUND`.
- `GatheringController.cancelGathering` — 더미 제거, `@AuthenticationPrincipal Jwt` 연결.

**참고**
- `gathering.cancel()`(상태 전이 도메인 메서드)과 `ErrorCode`(FORBIDDEN, GATHERING_NOT_MODIFIABLE, GATHERING_NOT_FOUND)는 앞선 이슈에서 이미 추가됨 → 이번 PR은 ErrorCode 변경 없음.
- 확정(CONFIRMED) 이후 취소 정책은 별도 협의(현재는 모집중만 취소 가능).

## 관련 이슈
Closes #(이슈6: 모임 취소 API 번호 입력)

## 테스트
- [ ] 로컬 환경에서 테스트 완료
- [ ] 기존 기능 정상 동작 확인

**확인 시나리오(제안)**
- 방장이 모집중 모임 취소 → 200, status=CANCELED, canceledAt 기록.
- 방장 아님 → 403 FORBIDDEN.
- 이미 CONFIRMED/CANCELED 등 → 409 GATHERING_NOT_MODIFIABLE.
- 없는 ID → 404 GATHERING_NOT_FOUND.
- 취소된 모임이 목록/상세에서 status=CANCELED 로 조회되는지 확인.

## 체크리스트
- [ ] 코드 리뷰 요청 전 셀프 리뷰 완료
- [ ] 불필요한 코드/주석 제거
- [ ] 커밋 메시지 컨벤션 준수
