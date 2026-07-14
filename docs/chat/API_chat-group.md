# 채팅(그룹) API 명세서

모임 확정 시 자동 생성되는 그룹 채팅방(`chatRoomType = GROUP`) 관련 API 명세입니다.
메시지 실시간 송수신은 **WebSocket(STOMP)**, 채팅방/이력 조회 및 폴백 전송은 **REST**로 제공합니다.

> 1차 단계에서는 실제 DB 저장 없이 더미 응답 기반 Swagger 명세로 구현합니다.

---

## 0. 공통 규칙

### 인증
로그인 이후 모든 API는 JWT Access Token을 사용합니다.

```
Authorization: Bearer {accessToken}
Content-Type: application/json
```

WebSocket도 STOMP `CONNECT` 프레임 헤더에 동일한 토큰을 실어 인증합니다.

```
Authorization: Bearer {accessToken}
```

### 공통 성공 응답

```json
{
  "success": true,
  "message": "요청이 성공했습니다.",
  "data": {}
}
```

### 공통 에러 응답

```json
{
  "success": false,
  "code": "ERROR_CODE",
  "message": "에러 메시지",
  "data": null
}
```

### 페이지네이션
목록 조회는 `page=0`, `size=20` 방식을 기본으로 하며, 응답에 `content`, `page`, `size`, `totalElements`, `totalPages`, `first`, `last`를 포함합니다.

### 포맷 규칙
- 날짜+시간: ISO 8601 (예: `2026-07-11T18:30:00+09:00`)
- 시간만: `HH:mm` (예: `18:30`)
- enum: ERD 정의 영문 값 그대로 사용

### 관련 Enum

| Enum | 값 |
| --- | --- |
| chatRoomType | `GROUP`, `QUICK_CONNECT` |
| messageType | `TEXT`, `IMAGE`, `SYSTEM` |

> 본 명세 범위는 `GROUP` 채팅이며, 메시지 타입은 `TEXT`·`SYSTEM`을 다룹니다. (`IMAGE`, `QUICK_CONNECT`는 후속 이슈)

---

## 1. REST API

### 1-1. 내 채팅방 목록 조회

내가 참여 중인 그룹 채팅방 목록을 최근 메시지 순으로 조회합니다.

```
GET /api/chat/rooms?page=0&size=20
Authorization: Bearer {accessToken}
```

**Query Parameters**

| 이름 | 타입 | 필수 | 기본값 | 설명 |
| --- | --- | --- | --- | --- |
| page | int | N | 0 | 페이지 번호 |
| size | int | N | 20 | 페이지 크기 |

**Response 200**

```json
{
  "success": true,
  "message": "채팅방 목록 조회에 성공했습니다.",
  "data": {
    "content": [
      {
        "roomId": 10,
        "gatheringId": 1,
        "type": "GROUP",
        "title": "인문X자연 해커톤 팀",
        "memberCount": 4,
        "lastMessage": "내일 7시에 봬요!",
        "lastMessageAt": "2026-07-13T21:10:00+09:00",
        "unreadCount": 2
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
  }
}
```

---

### 1-2. 채팅방 상세 조회

채팅방 기본 정보와 참여자 목록을 조회합니다.

```
GET /api/chat/rooms/{roomId}
Authorization: Bearer {accessToken}
```

**Path Parameters**

| 이름 | 타입 | 설명 |
| --- | --- | --- |
| roomId | Long | 채팅방 ID |

**Response 200**

```json
{
  "success": true,
  "message": "채팅방 조회에 성공했습니다.",
  "data": {
    "roomId": 10,
    "gatheringId": 1,
    "type": "GROUP",
    "title": "인문X자연 해커톤 팀",
    "createdAt": "2026-07-09T20:20:00+09:00",
    "members": [
      {
        "userId": 1,
        "nickname": "인준",
        "departmentName": "컴퓨터공학과",
        "campus": "NATURAL",
        "role": "HOST"
      },
      {
        "userId": 2,
        "nickname": "기획러",
        "departmentName": "경영학과",
        "campus": "HUMANITIES",
        "role": "MEMBER"
      }
    ]
  }
}
```

---

### 1-3. 채팅방 메시지 이력 조회

채팅방의 이전 메시지를 페이지네이션으로 조회합니다. (최신 → 과거 순)

```
GET /api/chat/rooms/{roomId}/messages?page=0&size=20
Authorization: Bearer {accessToken}
```

**Path / Query Parameters**

| 이름 | 위치 | 타입 | 필수 | 기본값 | 설명 |
| --- | --- | --- | --- | --- | --- |
| roomId | path | Long | Y | - | 채팅방 ID |
| page | query | int | N | 0 | 페이지 번호 |
| size | query | int | N | 20 | 페이지 크기 |

**Response 200**

```json
{
  "success": true,
  "message": "메시지 조회에 성공했습니다.",
  "data": {
    "content": [
      {
        "messageId": 105,
        "roomId": 10,
        "senderId": 1,
        "senderNickname": "인준",
        "type": "TEXT",
        "content": "내일 7시에 봬요!",
        "createdAt": "2026-07-13T21:10:00+09:00"
      },
      {
        "messageId": 100,
        "roomId": 10,
        "senderId": null,
        "senderNickname": null,
        "type": "SYSTEM",
        "content": "기획러님이 입장했습니다.",
        "createdAt": "2026-07-09T20:21:00+09:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 2,
    "totalPages": 1,
    "first": true,
    "last": true
  }
}
```

> `SYSTEM` 메시지는 `senderId`, `senderNickname`이 `null`입니다.

---

### 1-4. 메시지 전송 (REST 폴백)

WebSocket 연결이 불가능한 상황을 위한 폴백 전송 엔드포인트입니다. 실시간 연결이 정상일 때는 WebSocket 발행을 사용합니다.

```
POST /api/chat/rooms/{roomId}/messages
Authorization: Bearer {accessToken}
```

**Request Body**

```json
{
  "type": "TEXT",
  "content": "안녕하세요!"
}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| type | enum | Y | 메시지 타입 (`TEXT`) |
| content | String | Y | 메시지 본문 |

**Response 200**

```json
{
  "success": true,
  "message": "메시지가 전송되었습니다.",
  "data": {
    "messageId": 106,
    "roomId": 10,
    "senderId": 1,
    "senderNickname": "인준",
    "type": "TEXT",
    "content": "안녕하세요!",
    "createdAt": "2026-07-14T10:00:00+09:00"
  }
}
```

---

### 1-5. 메시지 읽음 처리

특정 메시지까지 읽음 처리하여 `unreadCount`를 갱신합니다.

```
POST /api/chat/rooms/{roomId}/read
Authorization: Bearer {accessToken}
```

**Request Body**

```json
{
  "lastReadMessageId": 106
}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| lastReadMessageId | Long | Y | 마지막으로 읽은 메시지 ID |

**Response 200**

```json
{
  "success": true,
  "message": "읽음 처리되었습니다.",
  "data": {
    "roomId": 10,
    "lastReadMessageId": 106,
    "unreadCount": 0
  }
}
```

---

## 2. WebSocket (STOMP) API

### 2-1. 연결

```
Endpoint: /ws-stomp   (SockJS 지원)
```

CONNECT 프레임 헤더에 JWT를 실어 인증합니다.

```
Authorization: Bearer {accessToken}
```

### 2-2. Destination 규칙

| 구분 | Destination | 설명 |
| --- | --- | --- |
| 구독(SUBSCRIBE) | `/sub/chat/rooms/{roomId}` | 해당 채팅방 실시간 메시지 수신 |
| 발행(SEND) | `/pub/chat/rooms/{roomId}/send` | 해당 채팅방으로 메시지 전송 |

### 2-3. 메시지 발행 페이로드

`SEND /pub/chat/rooms/{roomId}/send`

```json
{
  "type": "TEXT",
  "content": "안녕하세요!"
}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| type | enum | Y | 메시지 타입 (`TEXT`) |
| content | String | Y | 메시지 본문 |

> 발신자(senderId)는 서버가 STOMP 세션의 인증 정보에서 추출하므로 페이로드에 포함하지 않습니다.

### 2-4. 브로드캐스트 응답

구독 중인 클라이언트가 `/sub/chat/rooms/{roomId}`로 수신하는 메시지 포맷입니다. (REST 메시지 스키마와 동일)

```json
{
  "messageId": 107,
  "roomId": 10,
  "senderId": 1,
  "senderNickname": "인준",
  "type": "TEXT",
  "content": "안녕하세요!",
  "createdAt": "2026-07-14T10:05:00+09:00"
}
```

입장/퇴장 등 시스템 이벤트는 서버가 `SYSTEM` 타입으로 동일 채널에 브로드캐스트합니다.

```json
{
  "messageId": 108,
  "roomId": 10,
  "senderId": null,
  "senderNickname": null,
  "type": "SYSTEM",
  "content": "인준님이 나갔습니다.",
  "createdAt": "2026-07-14T10:06:00+09:00"
}
```

---

## 3. 모임 확정 연동

모임 확정 시 그룹 채팅방이 자동 생성되며, 확정 응답의 `chatRoomId`가 채팅방 ID입니다.

```
POST /api/gatherings/{gatheringId}/confirm
```

```json
{
  "success": true,
  "message": "모임이 확정되었습니다.",
  "data": {
    "gatheringId": 1,
    "status": "CONFIRMED",
    "confirmedAt": "2026-07-09T20:20:00+09:00",
    "chatRoomId": 10
  }
}
```

이후 클라이언트는 `chatRoomId`로 `/sub/chat/rooms/{chatRoomId}`를 구독하여 채팅에 참여합니다.

---

## 4. 에러 코드

공통 에러 코드에 더해 채팅 도메인에서 사용하는 코드입니다.

| 코드 | HTTP Status | 설명 |
| --- | --- | --- |
| UNAUTHORIZED | 401 | 로그인 필요 |
| EXPIRED_TOKEN | 401 | 토큰 만료 |
| FORBIDDEN | 403 | 채팅방 참여자가 아님 |
| CHAT_ROOM_NOT_FOUND | 404 | 채팅방을 찾을 수 없음 |
| MESSAGE_NOT_FOUND | 404 | 메시지를 찾을 수 없음 |
| INVALID_REQUEST | 400 | 요청 값이 올바르지 않음 |
| INTERNAL_SERVER_ERROR | 500 | 서버 내부 오류 |

---

## 5. 엔드포인트 요약

| Method | Path | 인증 | 설명 |
| --- | --- | --- | --- |
| GET | `/api/chat/rooms` | ✅ | 내 채팅방 목록 조회 |
| GET | `/api/chat/rooms/{roomId}` | ✅ | 채팅방 상세 조회 |
| GET | `/api/chat/rooms/{roomId}/messages` | ✅ | 메시지 이력 조회 |
| POST | `/api/chat/rooms/{roomId}/messages` | ✅ | 메시지 전송 (REST 폴백) |
| POST | `/api/chat/rooms/{roomId}/read` | ✅ | 읽음 처리 |
| SUBSCRIBE | `/sub/chat/rooms/{roomId}` | ✅ | 실시간 메시지 수신 |
| SEND | `/pub/chat/rooms/{roomId}/send` | ✅ | 실시간 메시지 전송 |
