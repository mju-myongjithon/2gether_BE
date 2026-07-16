# 이슈 명세서 — 모임(gathering) · 채팅(chat)

`.github/ISSUE_TEMPLATE/task.md` 형식(`[TASK]`)에 맞춘 기능별 이슈 모음. 각 블록을 GitHub 이슈로 그대로 복사해서 등록하면 됩니다. 기준 ERD는 `docs/ERD_개선안.dbml`, 캠퍼스 비율 기능은 제외.

**의존 순서(권장 진행)**: G1 → (G2~G7 병렬) → (G8~G10) / C1 → (C2~C5). G7(모임 확정)은 C1(채팅 기반)에 의존.

**공통 참고 (모든 이슈 적용)**
- 응답: `ApiResponse<T>`(성공) / `ErrorResponse`(실패), 목록은 `PageResponse<T>`.
- 예외: `BusinessException(ErrorCode)` → `GlobalExceptionHandler`. **필요한 ErrorCode는 `global/exception/ErrorCode.java`에 추가(추가만, 기존 값 수정 없음 — 팀 공유 파일이므로 커밋 시 알림)**.
- 인증 주체: `@AuthenticationPrincipal Jwt jwt` → `jwt.getSubject()`가 `auth_user_id`.
- DTO는 record + Bean Validation(`@Valid`). 엔티티 직접 노출 금지, `from/of` 팩토리 변환.
- 날짜/시간 ISO-8601, enum은 영문 값 그대로.

---

## [TASK] 모임 도메인 기반 구조 (엔티티 · 레포지토리)

**Labels**: task · gathering
**Branch**: `feat/gathering-domain`

### 작업 내용
> 모임 기능의 토대가 되는 JPA 엔티티와 리포지토리를 만든다. 현재 `GatheringController`는 전부 더미 응답 상태이므로, 이후 모든 모임 API가 이 구조 위에 올라간다.

### 작업 목표
> Gathering / GatheringMember / GatheringTag / GatheringImage 엔티티와 리포지토리가 준비되어, 이후 기능 이슈들이 서비스 로직 구현에만 집중할 수 있게 한다.

### 세부 작업
- [ ] `gathering.domain.Gathering` 엔티티 (host `@ManyToOne(LAZY)`, `category`/`status` `@Enumerated(STRING)`, `maxMembers`/`currentMembers`, 모집 일시 필드)
- [ ] `GatheringCategory` enum (STUDY, HOBBY, HACKATHON, PROJECT, NETWORKING)
- [ ] 상태 전이 도메인 메서드 (`confirm()`, `cancel()`, `increaseMember()`, `isRecruiting()`, `isHost()`)
- [ ] 파생 표시 상태 `@Transient displayStatus(now)` (상시/예정/모집중/완료)
- [ ] `GatheringMember`, `GatheringTag`, `GatheringImage` 엔티티
- [ ] 각 Repository + 유니크/인덱스 매핑 (`@Table(indexes=...)` 또는 DDL)
- [ ] `createdAt/updatedAt` 자동화 (`@PrePersist/@PreUpdate` 또는 Auditing)

### 참고 사항
> ERD: `gathering`, `gathering_member`, `gathering_tag`, `gathering_image`. FK 매핑은 host/member처럼 탐색이 잦은 곳만 `@ManyToOne(LAZY)`. `tags` 테이블은 읽기 전용으로만 참조.

---

## [TASK] 모임 생성 API

**Labels**: task · gathering
**Branch**: `feat/gathering-create`
**Depends on**: 모임 도메인 기반 구조

### 작업 내용
> 방장이 모임을 생성하는 API를 구현한다. 생성자는 자동으로 `gathering_member`에 HOST로 등록된다.

### 작업 목표
> `POST /api/gatherings` 호출 시 모임 + HOST 멤버 + 태그/이미지가 한 트랜잭션으로 저장되고, 더미 응답이 실제 저장 결과로 대체된다.

### 세부 작업
- [ ] `GatheringCreateRequest` 검증 추가 (`@NotBlank title`, `@Size`, `@Min(1) maxMembers`, `tagIds[]`, `imageUrls[]`)
- [ ] `GatheringService.create(authUserId, req)` — User 조회 → Gathering 저장 → HOST 멤버 저장 → 태그/이미지 저장
- [ ] 이미지 5장 이하 검증(API단), 존재하지 않는 `tagId` 검증
- [ ] 컨트롤러 더미 제거, `201 Created` 반환
- [ ] ErrorCode: `USER_NOT_FOUND`(기존), 필요 시 `INVALID_TAG`

### 참고 사항
> 응답: `{gatheringId, hostId, title, status, createdAt}`. `current_members`는 1(HOST)로 시작. `recruit_start_at`/`recruit_end_at`은 NULL 허용(즉시/상시 모집).

---

## [TASK] 모임 목록 조회 API (필터 · 검색 · 페이징)

**Labels**: task · gathering
**Branch**: `feat/gathering-list`
**Depends on**: 모임 도메인 기반 구조

### 작업 내용
> category/status/keyword 조건과 페이지네이션으로 모임 목록을 조회한다.

### 작업 목표
> `GET /api/gatherings`가 동적 조건 조합으로 실제 데이터를 페이징 반환하고, 카드에 필요한 host·현재인원·태그가 N+1 없이 채워진다.

### 세부 작업
- [ ] 동적 조건 쿼리 (QueryDSL 또는 JPA Specification): category, status, keyword(title/content)
- [ ] host `fetch join`, 현재 인원은 `current_members` 컬럼 사용
- [ ] 정렬 기준 고정 (`created_at DESC` 등) + `PageResponse` 매핑
- [ ] `GatheringSummaryResponse`에 태그 목록 포함(태그는 배치 `in` 조회)
- [ ] 파생 표시 상태(모집중/완료 등) 응답 필드 반영

### 참고 사항
> keyword는 `LIKE '%..%'`라 데이터 증가 시 인덱스 미탐 → 향후 `pg_trgm` GIN 고려(이슈로 분리 가능). 인덱스: `(status, category)`, `(created_at)`.

---

## [TASK] 모임 상세 조회 API

**Labels**: task · gathering
**Branch**: `feat/gathering-detail`
**Depends on**: 모임 도메인 기반 구조

### 작업 내용
> 모임 상세 정보 + 확정 멤버 목록 + 내 신청/참여 상태를 조회한다. (캠퍼스 비율은 제외)

### 작업 목표
> `GET /api/gatherings/{id}`가 상세/멤버/내 상태를 조립해 반환하고, 존재하지 않으면 404를 던진다.

### 세부 작업
- [ ] `findDetailById` (host fetch join)
- [ ] 멤버 목록 조회 (`GatheringMemberResponse`)
- [ ] 태그·이미지 목록 포함
- [ ] 로그인 사용자의 `myApplicationStatus`, `isHost`, `isMember` 계산
- [ ] `GatheringDetailResponse`에서 `campusRatio` 필드 **제거**
- [ ] ErrorCode: `GATHERING_NOT_FOUND`

### 참고 사항
> 더미 컨트롤러의 `CampusRatioResponse` 관련 코드 삭제. 비로그인 조회 허용 여부는 팀 정책에 따름(허용 시 내 상태 필드는 null).

---

## [TASK] 모임 수정 API

**Labels**: task · gathering
**Branch**: `feat/gathering-update`
**Depends on**: 모임 도메인 기반 구조

### 작업 내용
> 방장이 모집 중인 모임의 정보를 부분 수정한다.

### 작업 목표
> `PATCH /api/gatherings/{id}`가 방장·모집중 조건을 검증하고 변경 필드만 반영한다.

### 세부 작업
- [ ] `GatheringUpdateRequest` (부분 수정 필드, 검증)
- [ ] 권한 검증(방장만) + 상태 검증(RECRUITING만)
- [ ] 태그/이미지 재설정 로직(전체 교체 방식)
- [ ] 응답 `{gatheringId, updatedAt}`
- [ ] ErrorCode: `FORBIDDEN`, `GATHERING_NOT_MODIFIABLE`

### 참고 사항
> 태그/이미지는 부분 patch보다 "요청 목록으로 전체 교체"가 단순(availability와 동일 패턴).

---

## [TASK] 모임 취소 API

**Labels**: task · gathering
**Branch**: `feat/gathering-cancel`
**Depends on**: 모임 도메인 기반 구조

### 작업 내용
> 방장이 모집 중인 모임을 취소한다. 물리 삭제가 아니라 `status = CANCELED`.

### 작업 목표
> `POST /api/gatherings/{id}/cancel`가 소프트 취소로 처리되고 `canceled_at`이 기록된다.

### 세부 작업
- [ ] 권한(방장)·상태(RECRUITING) 검증
- [ ] `gathering.cancel()` 호출
- [ ] 응답 `{gatheringId, status, canceledAt}`
- [ ] ErrorCode: `FORBIDDEN`, `GATHERING_NOT_MODIFIABLE`

### 참고 사항
> 확정(CONFIRMED) 이후 취소 정책은 별도 협의(현재는 모집중만 취소).

---

## [TASK] 모임 확정 API (+ 그룹 채팅방 생성)

**Labels**: task · gathering · chat
**Branch**: `feat/gathering-confirm`
**Depends on**: 모임 도메인 기반 구조, 채팅 도메인 기반 구조

### 작업 내용
> 방장이 모집을 마감(확정)하면 `status = CONFIRMED`로 바뀌고 그룹 채팅방이 자동 생성되며 확정 멤버 전원이 참여자로 등록된다.

### 작업 목표
> `POST /api/gatherings/{id}/confirm` 한 번으로 확정 + 채팅방 + 멤버 등록 + 시스템 메시지가 한 트랜잭션으로 처리된다.

### 세부 작업
- [ ] 권한(방장)·상태(RECRUITING) 검증
- [ ] `gathering.confirm()` + `confirmed_at`
- [ ] `chat_room`(type=GROUP, gathering_id) 생성
- [ ] 확정 멤버 전원 `chat_room_member` 등록
- [ ] 입장/확정 SYSTEM 메시지 생성(선택)
- [ ] 응답 `{gatheringId, status, confirmedAt, chatRoomId}`
- [ ] ErrorCode: `FORBIDDEN`, `GATHERING_NOT_MODIFIABLE`

### 참고 사항
> gathering → chat 방향 호출. 채팅 기반 구조(C1) 완료 후 착수. `chat_room.gathering_id`는 UNIQUE(모임당 1개).

---

## [TASK] 모임 참가 신청 API

**Labels**: task · gathering
**Branch**: `feat/gathering-apply`
**Depends on**: 모임 도메인 기반 구조

### 작업 내용
> 사용자가 모집 중인 모임에 참가를 신청한다.

### 작업 목표
> `POST /api/gatherings/{id}/applications`가 중복/이미 멤버/모집마감을 걸러내고 PENDING 신청을 생성한다.

### 세부 작업
- [ ] `GatheringApplicationCreateRequest{message}` 검증
- [ ] 모집중 상태 검증, 본인=방장 신청 차단
- [ ] 중복 신청 방지(유니크 또는 부분 유니크 정책 반영), 이미 멤버 차단
- [ ] 신청 저장(status=PENDING, applied_at)
- [ ] 응답 `{applicationId, status}`
- [ ] ErrorCode: `GATHERING_NOT_RECRUITING`, `DUPLICATE_APPLICATION`, `ALREADY_MEMBER`

### 참고 사항
> REJECTED 후 재신청 허용 여부는 ERD 주석의 부분 유니크 정책으로 결정. 확정하고 이슈에 명시.

---

## [TASK] 모임 신청 관리 API (신청 목록 · 수락 · 거절)

**Labels**: task · gathering
**Branch**: `feat/gathering-application-manage`
**Depends on**: 모임 참가 신청 API

### 작업 내용
> 방장이 자기 모임의 신청자 목록을 보고 수락/거절한다. 수락 시 참여자로 등록된다.

### 작업 목표
> 신청 목록 조회 + 수락(멤버 생성·정원 체크·현재인원 증가) + 거절(사유 저장)이 방장 권한 하에 동작한다.

### 세부 작업
- [ ] `GET /api/gatherings/{id}/applications` (방장, 페이징, status 필터)
- [ ] `POST .../applications/{appId}/accept` — status=ACCEPTED + `gathering_member` 생성 + `current_members++` (한 트랜잭션)
- [ ] 정원 초과 검증(`current_members < max_members`), 동시성 대비(비관적 락 또는 조건부 UPDATE)
- [ ] `POST .../applications/{appId}/reject` + `rejectReason` 저장
- [ ] 권한(방장) 검증
- [ ] ErrorCode: `FORBIDDEN`, `CAPACITY_EXCEEDED`

### 참고 사항
> 인덱스: `(gathering_id, status)`(신청함), `(user_id, status)`(내 신청). 수락은 반드시 원자적 처리.

---

## [TASK] 모임 멤버 조회 API

**Labels**: task · gathering
**Branch**: `feat/gathering-members`
**Depends on**: 모임 도메인 기반 구조

### 작업 내용
> 특정 모임의 확정 참여자 목록을 조회한다.

### 작업 목표
> `GET /api/gatherings/{id}/members`가 role 포함 멤버 목록을 반환한다.

### 세부 작업
- [ ] 멤버 목록 조회(user join, N+1 주의)
- [ ] `GatheringMemberListResponse` / `GatheringMemberResponse` 매핑
- [ ] 정렬(HOST 우선 등)

### 참고 사항
> 상세 조회(G4)와 응답 재사용 가능. 캠퍼스 비율 제외이므로 campus 필드 없이 학과/역할 정도만.

---

## [TASK] 채팅 도메인 기반 구조 (엔티티 · 레포지토리)

**Labels**: task · chat
**Branch**: `feat/chat-domain`

### 작업 내용
> 채팅 기능의 토대인 ChatRoom / ChatRoomMember / Message 엔티티와 리포지토리를 만든다. 현재 채팅은 enum·DTO만 있고 엔티티가 없다.

### 작업 목표
> 채팅방·참여자·메시지 저장 구조가 준비되어 채팅 API와 모임 확정 연계(G7)가 올라갈 수 있다.

### 세부 작업
- [ ] `ChatRoom` 엔티티 (`type` STRING enum, `gathering_id` nullable+unique)
- [ ] `ChatRoomMember` (`last_read_message_id`, `left_at` nullable)
- [ ] `Message` (`sender` nullable=SYSTEM, `type`, `content`, `sent_at`)
- [ ] 각 Repository + 인덱스 (`message(chat_room_id, id)`, `chat_room_member(user_id)`)
- [ ] WebSocket 설정 확인(`WebSocketConfig` 기존)

### 참고 사항
> ERD: `chat_room`, `chat_room_member`, `message`. 메시지 조회는 커서(keyset) 페이징 전제로 `(chat_room_id, id)` 인덱스 필수.

---

## [TASK] 채팅방 목록 · 상세 조회 API

**Labels**: task · chat
**Branch**: `feat/chat-room-read`
**Depends on**: 채팅 도메인 기반 구조

### 작업 내용
> 내가 참여 중인 채팅방 목록과 특정 방의 상세(참여자 등)를 조회한다.

### 작업 목표
> 채팅방 목록에 최근 메시지·안 읽은 수 요약이 실리고, 상세에서 참여자를 확인할 수 있다.

### 세부 작업
- [ ] `GET /api/chat-rooms` — 내 방 목록(`chat_room_member.user_id`), 최근 메시지/미읽음 요약
- [ ] `GET /api/chat-rooms/{roomId}` — 방 상세 + 참여자
- [ ] 참여 권한 검증(멤버만 조회)
- [ ] `ChatRoomSummaryResponse` / `ChatRoomDetailResponse` 매핑
- [ ] ErrorCode: `CHAT_ROOM_NOT_FOUND`, `FORBIDDEN`

### 참고 사항
> 미읽음 수 = `last_read_message_id` 이후 메시지 count. 목록 성능 위해 방별 최근 메시지 조회 최적화 주의.

---

## [TASK] 메시지 전송 · 조회 API (WebSocket + REST)

**Labels**: task · chat
**Branch**: `feat/chat-message`
**Depends on**: 채팅 도메인 기반 구조

### 작업 내용
> STOMP로 실시간 메시지를 주고받고, 과거 메시지는 REST 커서 페이징으로 불러온다.

### 작업 목표
> 실시간 송수신 + 무한 스크롤(과거 메시지)이 동작하고, 메시지가 DB에 저장된다.

### 세부 작업
- [ ] `ChatStompController` 메시지 수신 → 저장 → 구독자 브로드캐스트
- [ ] `ChatMessageSendRequest`(TEXT/IMAGE) 검증, 참여자만 전송 가능
- [ ] `GET /api/chat-rooms/{roomId}/messages?cursor=&size=` 커서 페이징(`id` 기준 역순)
- [ ] SYSTEM 메시지 처리(sender null)
- [ ] `ChatMessageResponse` 매핑
- [ ] ErrorCode: `FORBIDDEN`, `CHAT_ROOM_NOT_FOUND`

### 참고 사항
> OFFSET 페이징 금지(뒤로 갈수록 느려짐) → `(chat_room_id, id)` keyset. 이미지 업로드 방식은 팀 스토리지 정책 따름.

---

## [TASK] 읽음 처리 & 안 읽은 메시지 수

**Labels**: task · chat
**Branch**: `feat/chat-read`
**Depends on**: 메시지 전송·조회 API

### 작업 내용
> 사용자가 방을 읽으면 마지막 읽은 메시지를 갱신하고, 안 읽은 메시지 수를 제공한다.

### 작업 목표
> `last_read_message_id` 갱신으로 방별 미읽음 배지가 정확히 계산된다.

### 세부 작업
- [ ] `POST /api/chat-rooms/{roomId}/read` (`ChatReadRequest{lastReadMessageId}`)
- [ ] `chat_room_member.last_read_message_id` 갱신
- [ ] 미읽음 수 계산 쿼리(이후 메시지 count)
- [ ] `ChatReadResponse` 매핑

### 참고 사항
> 목록 조회(C2)의 미읽음 요약과 계산 로직 공유.

---

## [TASK] 안심 커넥트 (Quick Connect) 코드 생성 · 입장

**Labels**: task · chat
**Branch**: `feat/quick-connect`
**Depends on**: 채팅 도메인 기반 구조

### 작업 내용
> 6자리 코드로 입장하는 즉석 채팅방(모임과 무관)을 생성하고, 코드로 입장한다.

### 작업 목표
> 코드 발급 → 만료/사용 상태 관리 → 코드 입장까지 동작한다.

### 세부 작업
- [ ] `POST /api/quick-connect` — `chat_room`(type=QUICK_CONNECT, gathering_id=NULL) + `quick_connect_code` 생성
- [ ] 6자리 코드 생성 + 만료(`expires_at`) 설정
- [ ] `POST /api/quick-connect/join` — 코드 검증(ACTIVE/만료/사용) → `chat_room_member` 등록
- [ ] 활성 코드 유일성(부분 유니크 정책 반영), 만료 처리
- [ ] ErrorCode: `INVALID_QUICK_CODE`, `EXPIRED_QUICK_CODE`, `ALREADY_USED_QUICK_CODE`

### 참고 사항
> `code` 전체 UNIQUE 대신 활성 코드만 유일(`WHERE status='ACTIVE'` 부분 유니크). 만료 스케줄러는 후속 이슈로 분리 가능.
