## 개요
> 이 PR에서 변경한 내용을 간략하게 설명해주세요.

`POST /api/gatherings/{id}/confirm` 모임 확정 API를 더미 응답에서 실제 DB 반영으로 교체했습니다.
방장이 모집을 마감하면 `status = CONFIRMED` + `confirmed_at` 이 기록됩니다.

**범위 제한**: 확정 규칙에 따라 이번 PR은 **상태 전이만** 구현합니다. 그룹 채팅방 생성·멤버 등록·시스템 메시지는 채팅(chat) 도메인 구축 이후 별도로 연계하며, 현재 응답의 `chatRoomId` 는 null 입니다.

## 변경 사항
- `GatheringService.confirm(authUserId, gatheringId)` 추가 — 방장 검증(`FORBIDDEN`) + 모집중 검증(`GATHERING_NOT_MODIFIABLE`) 후 `gathering.confirm()` 호출. 없는 모임이면 `GATHERING_NOT_FOUND`. 채팅방 미연계로 `chatRoomId=null`.
- `GatheringController.confirmGathering` — 더미 제거, `@AuthenticationPrincipal Jwt` 연결.
- `GatheringConfirmResponse.chatRoomId` — Swagger 설명에 "채팅 도메인 구축 전까지 null" 명시.
- `GatheringController` 더미가 모두 실제 구현으로 대체되어 미사용 import(GatheringStatus, OffsetDateTime) 정리.

**참고**
- `gathering.confirm()`(상태 전이)과 ErrorCode(FORBIDDEN/GATHERING_NOT_MODIFIABLE/GATHERING_NOT_FOUND)는 앞선 이슈에서 이미 추가됨 → 이번 PR은 ErrorCode 변경 없음.
- 후속(채팅 연계): 확정 시 `chat_room`(type=GROUP, gathering_id) 생성 + 확정 멤버 전원 `chat_room_member` 등록 + `chatRoomId` 채우기.

## 관련 이슈
Closes #(이슈7: 모임 확정 API 번호 입력)

## 테스트
- [ ] 로컬 환경에서 테스트 완료
- [ ] 기존 기능 정상 동작 확인

**확인 시나리오(제안)**
- 방장이 모집중 모임 확정 → 200, status=CONFIRMED, confirmedAt 기록, chatRoomId=null.
- 방장 아님 → 403 FORBIDDEN.
- 이미 CONFIRMED/CANCELED 등 → 409 GATHERING_NOT_MODIFIABLE.
- 없는 ID → 404 GATHERING_NOT_FOUND.
- 확정 후 수정/취소 시도 → 409(모집중 아님) 확인.

## 체크리스트
- [ ] 코드 리뷰 요청 전 셀프 리뷰 완료
- [ ] 불필요한 코드/주석 제거
- [ ] 커밋 메시지 컨벤션 준수
