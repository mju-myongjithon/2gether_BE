---
name: 작업
about: 채팅(그룹) 기능 API 명세 및 구현
title: "[TASK] 채팅(그룹) API 명세 구현"
labels: task
assignees: ''
---

## 작업 내용
> 수행할 작업을 설명해주세요.

모임 확정 시 자동 생성되는 **그룹 채팅방(chatRoomType = GROUP)** 기능의 백엔드 API를 명세하고 구현합니다.

메시지 송수신은 **WebSocket(STOMP)** 기반 실시간 통신으로 처리하고,
채팅방 목록·이전 메시지 조회 및 실시간 연결이 불가능한 상황을 대비한 **REST 폴백** 엔드포인트를 함께 제공합니다.

기존 gathering / gathering-application / gathering-member API와 동일하게,
1차 단계에서는 실제 DB 저장 없이 **더미 응답 기반 Swagger 명세**를 먼저 구현합니다.

## 작업 목표
> 이 작업을 통해 달성하고자 하는 목표를 적어주세요.

- 모임 확정 → 그룹 채팅방 자동 생성 흐름을 API로 정의한다.
- 프론트엔드가 채팅 화면을 붙일 수 있도록 채팅방/메시지 관련 요청·응답 스키마를 확정한다.
- WebSocket(STOMP) 목적지(destination) 규칙과 메시지 페이로드 포맷을 문서로 공유한다.
- REST 폴백(채팅방 목록, 메시지 이력, 메시지 전송)을 정의해 실시간 연결 실패 시에도 동작을 보장한다.

## 세부 작업
- [ ] `chat` 도메인 패키지 생성 (`controller`, `domain`, `dto/request`, `dto/response`)
- [ ] `ChatRoomType`(GROUP, QUICK_CONNECT), `MessageType`(TEXT, IMAGE, SYSTEM) enum 정의
- [ ] REST: 내 채팅방 목록 조회 `GET /api/chat/rooms`
- [ ] REST: 채팅방 상세(참여자 포함) 조회 `GET /api/chat/rooms/{roomId}`
- [ ] REST: 채팅방 메시지 이력 조회(페이지네이션) `GET /api/chat/rooms/{roomId}/messages`
- [ ] REST: 메시지 전송 폴백 `POST /api/chat/rooms/{roomId}/messages`
- [ ] REST: 메시지 읽음 처리 `POST /api/chat/rooms/{roomId}/read`
- [ ] WebSocket: STOMP 엔드포인트/구독·발행 destination 규칙 정의
- [ ] WebSocket: 메시지 발행 페이로드 및 브로드캐스트 응답 스키마 정의
- [ ] 모임 확정(`POST /api/gatherings/{gatheringId}/confirm`)과의 연동(`chatRoomId` 반환) 확인
- [ ] Swagger 문서에 `채팅 API` 태그로 노출
- [ ] PR용 API 명세서(`docs/chat/API_chat-group.md`) 작성

## 참고 사항
> 작업 시 참고할 내용이 있다면 적어주세요.

- 공통 성공/에러 응답 형식, 공통 에러 코드, 페이지네이션 규칙(page=0, size=20)은 프로젝트 공통 규칙을 따른다.
- 날짜+시간은 ISO 8601(`2026-07-11T18:30:00+09:00`), 시간만 있는 값은 `HH:mm` 포맷을 사용한다.
- enum은 ERD에 정의된 영문 값을 그대로 사용한다.
- 이번 이슈 범위는 **그룹 채팅(GROUP)** 이며, 퀵 커넥트(QUICK_CONNECT)·이미지 업로드는 후속 이슈로 분리한다.
- SYSTEM 메시지(입장/퇴장 안내 등)는 그룹 채팅 응답 스키마에 포함하되, 발행은 서버 내부 이벤트로 처리한다.
- 인증: 로그인 이후 API는 JWT Access Token(`Authorization: Bearer {accessToken}`)을 사용하며, WebSocket 연결(CONNECT) 시에도 동일 토큰으로 인증한다.
