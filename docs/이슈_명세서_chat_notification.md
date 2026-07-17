# 이슈 명세서 — 채팅(chat) · 알림(notification)

`.github/ISSUE_TEMPLATE/task.md` 형식(`[TASK]`)에 맞춘 기능별 이슈 모음. 각 블록을 GitHub 이슈로 그대로 복사해 등록하면 됩니다. 기준 ERD는 `docs/유대감_전체_ERD_합본.dbml`.

**아키텍처 전제**: 채팅은 **자체 WebSocket(STOMP)** 으로 구현. 텔레그램은 채팅이 아니라 **알림 발송 채널**로만 사용. AI는 별도(OpenAI) — 지금 범위 아님(메시지 `type=CARD` + `meta` 훅만 유지).

**의존 순서(권장)**: C1 → (C2~C8) / C1 완료 후 G7(모임 확정→채팅방 생성) 연결 / N1 → N2 → N3.

**공통 참고 (모든 이슈 적용)**
- 응답 `ApiResponse<T>` / 실패 `ErrorResponse` / 목록 `PageResponse<T>` 또는 커서.
- 예외 `BusinessException(ErrorCode)` → `GlobalExceptionHandler`. 필요한 ErrorCode는 `global/exception/ErrorCode.java`에 **추가만**(공유 파일, 커밋 시 공유).
- 인증 주체 `@AuthenticationPrincipal Jwt jwt` → `jwt.getSubject()` = auth_user_id.
- DTO는 record + Bean Validation. 엔티티 직접 노출 금지, `from/of` 변환.
- FK는 탐색 잦은 곳만 `@ManyToOne(LAZY)`, enum은 `@Enumerated(STRING)`.
- 외부 테이블(users, gathering)은 참조만, 수정 금지.

---

## [TASK] 채팅 도메인 기반 구조 (엔티티 · 레포지토리)

**Labels**: task · chat
**Branch**: `feat/chat-domain`

### 작업 내용
> 채팅 기능의 토대인 엔티티/리포지토리를 만든다. 현재 채팅은 enum·DTO만 있고 엔티티가 없다.

### 작업 목표
> ChatRoom / ChatRoomMember / Message / MessageAttachment / ChatNotice 엔티티와 리포지토리가 준비되어, 이후 채팅 기능 이슈가 서비스 로직에 집중할 수 있다.

### 세부 작업
- [ ] `ChatRoom` (type `@Enumerated(STRING)`, gathering_id nullable+unique, last_message_id/at 캐시, closed_at)
- [ ] `ChatRoomMember` (role, last_read_message_id, notification_enabled, joined_at, left_at)
- [ ] `Message` (sender nullable, type TEXT/IMAGE/SYSTEM/CARD, content, `meta` jsonb, deleted_at, client_message_id)
- [ ] `MessageAttachment`, `ChatNotice`
- [ ] 각 Repository + 인덱스(`message(chat_room_id, id)`, `chat_room_member(user_id)`, unique(chat_room_id,user_id))
- [ ] `meta` JSONB 매핑(Hibernate `@JdbcTypeCode(SqlTypes.JSON)` 또는 컨버터)

### 참고 사항
> ERD: chat_room, chat_room_member, message, message_attachment, chat_notice. 메시지 조회는 커서 페이징 전제로 `(chat_room_id, id)` 인덱스 필수. `type=CARD`/`meta`는 AI 공유 대비 훅(지금 로직 구현 안 함).

---

## [TASK] 모임 확정 시 그룹 채팅방 자동 생성 (gathering 연계)

**Labels**: task · chat · gathering
**Branch**: `feat/chat-auto-create`
**Depends on**: 채팅 도메인 기반 구조

### 작업 내용
> 모임이 확정(CONFIRMED)되면 GROUP 채팅방을 만들고 확정 멤버 전원을 참여자로 등록한다.

### 작업 목표
> gathering confirm 트랜잭션에서 `ChatRoomService.createGroupRoom(gathering)`를 호출해 방+멤버+시스템 메시지가 원자적으로 생성된다.

### 세부 작업
- [ ] `ChatRoomService.createGroupRoom(gatheringId, memberUserIds)` — type=GROUP, gathering_id 세팅
- [ ] 확정 멤버 전원 `chat_room_member` 등록(방장 role=OWNER)
- [ ] 생성/입장 SYSTEM 메시지 발행
- [ ] gathering confirm(G7)에서 이 서비스 호출(같은 트랜잭션)
- [ ] `chat_room.gathering_id` UNIQUE로 중복 생성 방지

### 참고 사항
> gathering 이슈 G7과 짝. chat 쪽은 "서비스 메서드 제공"까지, 호출은 gathering confirm에서.

---

## [TASK] 채팅방 목록 · 상세 조회 API

**Labels**: task · chat
**Branch**: `feat/chat-room-read`
**Depends on**: 채팅 도메인 기반 구조

### 작업 내용
> 내가 참여 중인 채팅방 목록(최근순 + 마지막 메시지 미리보기 + 안읽음 수)과 방 상세(참여자)를 조회한다.

### 작업 목표
> `GET /api/chat-rooms`가 `last_message_at` 최근순으로, 미읽음 수까지 실어 반환한다.

### 세부 작업
- [ ] `GET /api/chat-rooms` — `chat_room_member(user_id, left_at IS NULL)` → chat_room join, order by last_message_at desc
- [ ] 마지막 메시지 미리보기(`chat_room.last_message_id`), 미읽음 수(= last_message_id 이후 개수 or last_read 비교)
- [ ] `GET /api/chat-rooms/{roomId}` — 상세 + 참여자, 참여 권한 검증
- [ ] `ChatRoomSummaryResponse` / `ChatRoomDetailResponse`
- [ ] ErrorCode: `CHAT_ROOM_NOT_FOUND`, `FORBIDDEN`

### 참고 사항
> last_message_* 캐시 덕에 목록에서 message 집계 불필요. 미읽음은 chat_room_member.last_read_message_id 기준.

---

## [TASK] 메시지 전송 · 조회 (WebSocket STOMP + REST 커서 페이징)

**Labels**: task · chat
**Branch**: `feat/chat-message`
**Depends on**: 채팅 도메인 기반 구조

### 작업 내용
> STOMP로 실시간 송수신하고, 과거 메시지는 REST 커서 페이징으로 불러온다. 메시지 저장 시 방 last_message 캐시를 갱신한다.

### 작업 목표
> 실시간 채팅 + 무한 스크롤이 동작하고, 저장/브로드캐스트/캐시 갱신/멱등 처리가 일관된다.

### 세부 작업
- [ ] `ChatStompController` 수신 → 저장 → 구독자 브로드캐스트(`/topic/rooms/{id}`)
- [ ] `ChatMessageSendRequest`(TEXT/IMAGE) 검증, 참여자만 전송
- [ ] 저장 시 `chat_room.last_message_id/at` 갱신 + `client_message_id` 멱등 처리
- [ ] `GET /api/chat-rooms/{roomId}/messages?cursor=&size=` — `(chat_room_id, id)` keyset 역순
- [ ] SYSTEM 메시지 sender NULL 처리
- [ ] 메시지 저장 후 `MessageCreatedEvent` 발행(알림 연계용)
- [ ] ErrorCode: `FORBIDDEN`, `CHAT_ROOM_NOT_FOUND`

### 참고 사항
> `WebSocketConfig`(기존) 활용. OFFSET 페이징 금지 → keyset. 알림(N3)이 이 이벤트를 구독.

---

## [TASK] 읽음 처리 & 안 읽은 메시지 수

**Labels**: task · chat
**Branch**: `feat/chat-read`
**Depends on**: 메시지 전송·조회

### 작업 내용
> 방을 읽으면 마지막 읽은 메시지를 갱신하고 미읽음 수를 계산한다.

### 작업 목표
> `last_read_message_id` 갱신으로 방별 미읽음 배지가 정확히 계산된다.

### 세부 작업
- [ ] `POST /api/chat-rooms/{roomId}/read` (`{lastReadMessageId}`)
- [ ] `chat_room_member.last_read_message_id` 갱신
- [ ] 미읽음 수 계산(이후 메시지 count) — 목록(C3)과 로직 공유
- [ ] `ChatReadResponse`

### 참고 사항
> 단체방 "N명 읽음"은 MVP 제외(방 단위 포인터만). 필요 시 후속 이슈.

---

## [TASK] 이미지 메시지

**Labels**: task · chat
**Branch**: `feat/chat-image`
**Depends on**: 메시지 전송·조회

### 작업 내용
> 이미지 첨부 메시지를 전송/표시한다.

### 작업 목표
> IMAGE 메시지가 첨부 메타(URL·썸네일·크기)와 함께 저장·렌더된다.

### 세부 작업
- [ ] 이미지 업로드(스토리지 정책 확인) → URL 확보
- [ ] `type=IMAGE` 메시지 + `message_attachment` 저장(다중/sort_order)
- [ ] 썸네일/용량 검증
- [ ] 조회 응답에 attachment 포함

### 참고 사항
> content 겸용 금지, attachment 테이블 사용. 업로드 방식은 팀 스토리지(S3/Supabase Storage 등) 정책 따름.

---

## [TASK] 채팅방 공지

**Labels**: task · chat
**Branch**: `feat/chat-notice`
**Depends on**: 채팅 도메인 기반 구조

### 작업 내용
> 방장이 공지를 등록/해제하고, 상단 고정 공지를 조회한다.

### 작업 목표
> 방당 활성 공지 1건이 상단에 노출되고, OWNER만 등록할 수 있다.

### 세부 작업
- [ ] `POST /api/chat-rooms/{roomId}/notices` (OWNER 권한)
- [ ] 활성 공지 유일화(기존 활성 해제 후 신규 활성) — 부분 유니크(WHERE is_active)
- [ ] `GET`(상세에 활성 공지 포함), 해제 API
- [ ] 공지 등록 시 SYSTEM/CHAT_NOTICE 알림 연계(선택)
- [ ] ErrorCode: `FORBIDDEN`

### 참고 사항
> chat_notice 테이블. 권한은 chat_room_member.role=OWNER.

---

## [TASK] 채팅방 나가기

**Labels**: task · chat
**Branch**: `feat/chat-leave`
**Depends on**: 채팅 도메인 기반 구조

### 작업 내용
> 사용자가 채팅방을 나간다(소프트).

### 작업 목표
> `left_at` 세팅으로 나가되 메시지 이력은 보존되고, 목록/조회에서 제외된다.

### 세부 작업
- [ ] `POST /api/chat-rooms/{roomId}/leave` → `left_at = now()`
- [ ] 목록/상세 조회에서 `left_at IS NULL` 필터
- [ ] 퇴장 SYSTEM 메시지 발행
- [ ] (선택) 방장 나가기 시 위임/방 종료 정책

### 참고 사항
> 하드 삭제 안 함. 재입장 시 left_at 초기화 정책 결정.

---

## [TASK] 안심 커넥트 (Quick Connect)

**Labels**: task · chat
**Branch**: `feat/quick-connect`
**Depends on**: 채팅 도메인 기반 구조

### 작업 내용
> 6자리 코드로 입장하는 즉석 채팅방(모임 무관)을 생성/입장한다.

### 작업 목표
> 코드 발급 → 만료/사용 상태 관리 → 코드 입장이 동작한다.

### 세부 작업
- [ ] `POST /api/quick-connect` — chat_room(type=QUICK_CONNECT, gathering_id=NULL) + quick_connect_code 생성
- [ ] 6자리 코드 생성 + `expires_at`
- [ ] `POST /api/quick-connect/join` — 코드 검증(ACTIVE/만료/사용) → chat_room_member 등록
- [ ] 활성 코드 유일성(부분 유니크 WHERE status=ACTIVE), 만료 처리
- [ ] ErrorCode: `INVALID_QUICK_CODE`, `EXPIRED_QUICK_CODE`, `ALREADY_USED_QUICK_CODE`

### 참고 사항
> 메시지 자동삭제(종료 시)는 후속/Redis TTL 논의. 지금은 방·코드 상태 관리까지.

---

## [TASK] 알림 도메인 기반 구조 (인앱 알림함)

**Labels**: task · notification
**Branch**: `feat/notification-domain`

### 작업 내용
> 이벤트성 알림을 저장·조회하는 독립 알림 도메인을 만든다.

### 작업 목표
> Notification 엔티티/레포와 조회·읽음 API가 준비되어, 각 도메인이 이벤트만 발행하면 알림이 쌓인다.

### 세부 작업
- [ ] `notification.domain.Notification`, `NotificationType` enum
- [ ] `NotificationRepository` + 인덱스 `(user_id, created_at)`, `(user_id, read_at)`
- [ ] `GET /api/notifications` (페이징), `POST /api/notifications/{id}/read`, `POST /api/notifications/read-all`
- [ ] 미읽음 수 `GET /api/notifications/unread-count`
- [ ] `NotificationResponse`

### 참고 사항
> 채팅 메시지는 여기 안 쌓음(폭주 방지). 신청/수락/확정/공지 등 이벤트성만.

---

## [TASK] 텔레그램 연결 (user_telegram)

**Labels**: task · notification
**Branch**: `feat/telegram-link`
**Depends on**: 알림 도메인 기반 구조

### 작업 내용
> 사용자가 텔레그램 봇을 연결해 알림을 받을 수 있게 한다.

### 작업 목표
> 사용자 ↔ 텔레그램 chat_id 매핑이 저장되어, 알림 발송 시 대상 chat_id를 찾을 수 있다.

### 세부 작업
- [ ] `user_telegram` 엔티티/레포
- [ ] 봇 연결 플로우: 딥링크/코드로 봇 `/start` → 우리 서버가 chat_id 수신·저장(webhook 또는 폴링)
- [ ] `GET /api/me/telegram`(연결 상태), `DELETE`(연결 해제)
- [ ] 텔레그램 봇 토큰/설정(환경변수)

### 참고 사항
> ⚠️ 봇은 사용자가 먼저 시작해야 발송 가능. 미연결자는 인앱 알림만 수신. 봇 토큰은 `.env` 관리.

---

## [TASK] 이벤트 기반 알림 발송 (인앱 + 텔레그램)

**Labels**: task · notification
**Branch**: `feat/notification-dispatch`
**Depends on**: 알림 도메인 기반 구조, 텔레그램 연결

### 작업 내용
> 도메인 이벤트를 구독해 알림을 저장하고, 인앱(WebSocket)과 텔레그램으로 발송한다.

### 작업 목표
> 신청/수락/확정/공지/새 메시지 등 이벤트 발생 시 알림이 저장·발송되며, 뮤트·디바운스로 스팸을 막는다.

### 세부 작업
- [ ] 도메인 이벤트 정의·발행(gathering/chat) → `@EventListener` 수신(`@Async`)
- [ ] `notification` 저장 + WebSocket 인앱 푸시(`/user/{id}/queue/notifications`)
- [ ] `user_telegram` 있으면 Telegram Bot API 발송(접속 여부 무관)
- [ ] **뮤트 존중**: `chat_room_member.notification_enabled=false` 스킵, 본인(sender) 제외
- [ ] **디바운스/rate-limit**: 방·유저 단위로 묶어 "새 메시지 N개" 발송
- [ ] 발송 실패 시 로깅/재시도(텔레그램 장애가 채팅 흐름 막지 않도록 비동기·격리)

### 참고 사항
> 채팅 코어(메시지 저장)와 분리된 리스너 → 결합도 0. 나중에 이벤트를 Kafka로 옮기면 이 리스너가 컨슈머가 됨(테이블 무변경).
