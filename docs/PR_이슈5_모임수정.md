## 개요
> 이 PR에서 변경한 내용을 간략하게 설명해주세요.

`PATCH /api/gatherings/{id}` 모임 수정 API를 더미 응답에서 실제 DB 반영으로 교체했습니다.
방장이 **모집 중(RECRUITING)** 인 모임을 **부분 수정**하며, 보낸 필드만 반영하고 태그·이미지는 목록 전체 교체 방식입니다.

## 부분 수정(PATCH) 동작 규칙 — 자세히

이 API는 "바꾼 필드만 보내는" 방식입니다. 규칙은 아래와 같습니다.

**1) 일반 필드 (title, content, category, location, maxMembers, fusionEnabled, meetAt)**
- 요청에 **없으면(null)** → 기존 값 그대로 유지
- 요청에 **있으면** → 그 값으로 교체

예시 — 현재 모임이 `{title: "A모임", location: "인문캠", maxMembers: 6}` 일 때:
```json
PATCH /api/gatherings/1
{ "title": "A모임(수정)" }
```
→ 결과: title 만 "A모임(수정)"으로 바뀌고, location·maxMembers 등 나머지는 그대로.

**2) 태그·이미지 (tagIds, imageUrls)** — 여러 개라서 "통째 교체" 방식
- **미전송(null)** → 기존 태그/이미지 유지
- **목록 전송** → 보낸 목록으로 통째 교체
- **빈 목록 `[]` 전송** → 전부 삭제

예시:
```json
{ "tagIds": [1, 2] }   // 태그가 정확히 1,2번 두 개로 교체됨
{ "tagIds": [] }        // 태그 전부 삭제
// tagIds 자체를 안 보내면 → 기존 태그 유지
```

**3) 안전장치**
- `maxMembers` 를 **현재 참여 인원보다 적게** 줄이려 하면 400 `INVALID_REQUEST` (예: 이미 4명 참여 중인데 정원을 3으로 축소 불가).
- 존재하지 않는 `tagId` 가 포함되면 400 `INVALID_TAG`.

**4) 제약(MVP)**
- null 을 "값 비우기"로 해석하지 않습니다. 즉 meetAt·location 등을 **비우는(clear)** 동작은 현재 미지원이며, 필요해지면 별도 이슈로 처리합니다.

## 변경 사항
- `GatheringUpdateRequest` — 부분 수정용으로 개편. 전 필드 선택값(`Integer maxMembers`, `Boolean fusionEnabled`, `category` String), `tagIds`·`imageUrls` 추가. 검증(@Size/@Min)은 값이 있을 때만 적용.
- `Gathering.update(...)` 도메인 메서드 추가 — 최종값 반영 + `updatedAt` 갱신(정원 하한 재검증 포함).
- `GatheringService.update(authUserId, id, req)` 추가 — 방장/모집중 검증 → null=유지 규칙으로 필드 해석 → 태그·이미지 전체 교체(`deleteAllByGatheringId` 후 재삽입, 기존 `saveTags/saveImages` 재사용).
- `GatheringController.updateGathering` — 더미 제거, `@AuthenticationPrincipal Jwt` + `@Valid` 연결.
- `ErrorCode` — `FORBIDDEN(403)`, `GATHERING_NOT_MODIFIABLE(409)` 추가(값 추가만).

## 관련 이슈
Closes #38

## 테스트
- [ ] 로컬 환경에서 테스트 완료
- [ ] 기존 기능 정상 동작 확인

**확인 시나리오(제안)**
- 방장이 title 만 전송 → title 만 변경, 나머지 유지, `updatedAt` 갱신.
- `tagIds:[1,2]` → 태그 2개로 교체 / `tagIds:[]` → 태그 전체 삭제 / 미전송 → 유지.
- 방장 아님 → 403 FORBIDDEN, 모집중 아님(CONFIRMED 등) → 409 GATHERING_NOT_MODIFIABLE, 없는 ID → 404.
- maxMembers 를 현재 인원 미만으로 → 400 INVALID_REQUEST.

## 체크리스트
- [ ] 코드 리뷰 요청 전 셀프 리뷰 완료
- [ ] 불필요한 코드/주석 제거
- [ ] 커밋 메시지 컨벤션 준수
