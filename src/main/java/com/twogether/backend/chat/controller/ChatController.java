package com.twogether.backend.chat.controller;

import com.twogether.backend.chat.dto.request.ChatMessageSendRequest;
import com.twogether.backend.chat.dto.request.ChatReadRequest;
import com.twogether.backend.chat.dto.request.ChatNoticeCreateRequest;
import com.twogether.backend.chat.dto.request.ImageMessageSendRequest;
import com.twogether.backend.chat.dto.request.ShareTopicRecommendationRequest;
import com.twogether.backend.chat.dto.request.ShareMissionRecommendationRequest;
import com.twogether.backend.chat.dto.response.ChatMessagePageResponse;
import com.twogether.backend.chat.dto.response.ChatMessageResponse;
import com.twogether.backend.chat.dto.response.ChatNoticeResponse;
import com.twogether.backend.chat.dto.response.ChatReadResponse;
import com.twogether.backend.chat.dto.response.ChatRoomDetailResponse;
import com.twogether.backend.chat.dto.response.ChatRoomSummaryResponse;
import com.twogether.backend.chat.dto.response.ChatRoomActivityResponse;
import com.twogether.backend.chat.dto.response.TopicRecommendationResponse;
import com.twogether.backend.chat.dto.response.MissionRecommendationResponse;
import com.twogether.backend.chat.service.ChatRoomActivityService;
import com.twogether.backend.chat.service.ChatMessageService;
import com.twogether.backend.chat.service.ChatNoticeService;
import com.twogether.backend.chat.service.ChatRoomService;
import com.twogether.backend.chat.service.TopicRecommendationService;
import com.twogether.backend.chat.service.MissionRecommendationService;
import com.twogether.backend.global.response.ApiResponse;
import com.twogether.backend.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "채팅 API",
        description = "채팅방 목록/상세 조회, 메시지 이력 조회(커서), 메시지 전송(REST 폴백), 읽음 처리 관련 API"
)
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/chat-rooms")
public class ChatController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;
    private final ChatNoticeService chatNoticeService;
    private final ChatRoomActivityService chatRoomActivityService;
    private final TopicRecommendationService topicRecommendationService;
    private final MissionRecommendationService missionRecommendationService;

    public ChatController(
            ChatRoomService chatRoomService,
            ChatMessageService chatMessageService,
            ChatNoticeService chatNoticeService,
            ChatRoomActivityService chatRoomActivityService,
            TopicRecommendationService topicRecommendationService,
            MissionRecommendationService missionRecommendationService
    ) {
        this.chatRoomService = chatRoomService;
        this.chatMessageService = chatMessageService;
        this.chatNoticeService = chatNoticeService;
        this.chatRoomActivityService = chatRoomActivityService;
        this.topicRecommendationService = topicRecommendationService;
        this.missionRecommendationService = missionRecommendationService;
    }

    @Operation(
            summary = "AI 대화 주제 추천",
            description = "모임 정보와 태그, 현재 참여자의 관심 태그를 사용하며 채팅 메시지 본문은 사용하지 않습니다. 현재는 Mock AI Client를 사용합니다."
    )
    @PostMapping("/{chatRoomId}/topic-recommendations")
    public ResponseEntity<ApiResponse<TopicRecommendationResponse>> recommendTopics(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long chatRoomId
    ) {
        TopicRecommendationResponse response = topicRecommendationService.recommend(jwt.getSubject(), chatRoomId);
        return ResponseEntity.ok(ApiResponse.success("대화 주제 추천에 성공했습니다.", response));
    }

    @Operation(
            summary = "추천 대화 주제 공유",
            description = "추천된 대화 주제를 현재 채팅방에 CARD 메시지로 공유합니다. 현재 참여자만 호출할 수 있습니다."
    )
    @PostMapping("/{chatRoomId}/topic-recommendations/share")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> shareTopic(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long chatRoomId,
            @Valid @RequestBody ShareTopicRecommendationRequest request
    ) {
        ChatMessageResponse response = topicRecommendationService.share(jwt.getSubject(), chatRoomId, request);
        return ResponseEntity.ok(ApiResponse.success("추천 대화 주제를 공유했습니다.", response));
    }

    @Operation(
            summary = "AI 모임 미션 추천",
            description = """
                    모임 제목, 소개, 카테고리, 태그와 현재 참여자의 관심 태그를 기반으로 행동형 미션 1~3개를 추천합니다.
                    채팅 메시지 본문은 사용하지 않으며 현재는 외부 호출 없는 Mock AI Client를 사용합니다.
                    leftAt이 없는 현재 채팅방 참여자만 호출할 수 있습니다.
                    """
    )
    @PostMapping("/{chatRoomId}/mission-recommendations")
    public ResponseEntity<ApiResponse<MissionRecommendationResponse>> recommendMissions(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long chatRoomId
    ) {
        MissionRecommendationResponse response = missionRecommendationService.recommend(jwt.getSubject(), chatRoomId);
        return ResponseEntity.ok(ApiResponse.success("모임 미션 추천에 성공했습니다.", response));
    }

    @Operation(
            summary = "추천 모임 미션 공유",
            description = """
                    추천된 미션을 현재 채팅방에 CARD 메시지로 공유합니다.
                    현재 참여자만 호출할 수 있으며 기존 메시지 저장, WebSocket 및 이벤트 발행 흐름을 재사용합니다.
                    """
    )
    @PostMapping("/{chatRoomId}/mission-recommendations/share")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> shareMission(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long chatRoomId,
            @Valid @RequestBody ShareMissionRecommendationRequest request
    ) {
        ChatMessageResponse response = missionRecommendationService.share(jwt.getSubject(), chatRoomId, request);
        return ResponseEntity.ok(ApiResponse.success("추천 모임 미션을 공유했습니다.", response));
    }

    @Operation(
            summary = "채팅방 활동 통계 조회",
            description = "최근 7일 메시지 수 기반 활동 상태와 최근 30일의 날짜별 히트맵 데이터를 조회합니다. 메시지 내용은 조회하거나 반환하지 않습니다."
    )
    @GetMapping("/{chatRoomId}/activity")
    public ResponseEntity<ApiResponse<ChatRoomActivityResponse>> getActivity(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "채팅방 ID", example = "1") @PathVariable Long chatRoomId
    ) {
        ChatRoomActivityResponse response = chatRoomActivityService.getActivity(jwt.getSubject(), chatRoomId);
        return ResponseEntity.ok(ApiResponse.success("채팅방 활동 통계 조회에 성공했습니다.", response));
    }

    @Operation(
            summary = "내 채팅방 목록 조회",
            description = """
                    내가 참여 중인 채팅방 목록을 마지막 메시지 최근순으로 조회합니다.

                    각 항목에 마지막 메시지 미리보기와 안읽음 수를 함께 반환하며,
                    페이지네이션은 page=0, size=20 방식을 기본으로 합니다.
                    """
    )
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ChatRoomSummaryResponse>>> getChatRooms(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<ChatRoomSummaryResponse> response =
                chatRoomService.getMyChatRooms(jwt.getSubject(), page, size);

        return ResponseEntity.ok(
                ApiResponse.success("채팅방 목록 조회에 성공했습니다.", response)
        );
    }

    @Operation(
            summary = "채팅방 상세 조회",
            description = """
                    채팅방 기본 정보와 참여자 목록을 조회합니다.

                    참여 중인 사용자만 조회할 수 있으며, 그 외에는 403(FORBIDDEN)을 반환합니다.
                    """
    )
    @GetMapping("/{roomId}")
    public ResponseEntity<ApiResponse<ChatRoomDetailResponse>> getChatRoom(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long roomId
    ) {
        ChatRoomDetailResponse response =
                chatRoomService.getChatRoomDetail(jwt.getSubject(), roomId);

        return ResponseEntity.ok(
                ApiResponse.success("채팅방 조회에 성공했습니다.", response)
        );
    }

    @Operation(
            summary = "채팅방 메시지 이력 조회 (커서 페이징)",
            description = """
                    채팅방의 이전 메시지를 커서 페이징으로 조회합니다. (최신 → 과거 순)

                    첫 조회는 cursor 를 비워 호출하고, 응답의 nextCursor 를 다음 요청의 cursor 로 전달합니다.
                    nextCursor 가 null 이면 더 이상 과거 메시지가 없습니다.
                    SYSTEM 메시지는 senderId, senderNickname 이 null 입니다.
                    """
    )
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<ApiResponse<ChatMessagePageResponse>> getMessages(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long roomId,
            @Parameter(description = "커서(직전 응답의 nextCursor). 첫 조회는 생략")
            @RequestParam(required = false) Long cursor,
            @Parameter(description = "페이지 크기(기본 20, 최대 100)")
            @RequestParam(defaultValue = "20") int size
    ) {
        ChatMessagePageResponse response =
                chatMessageService.getMessages(jwt.getSubject(), roomId, cursor, size);

        return ResponseEntity.ok(
                ApiResponse.success("메시지 조회에 성공했습니다.", response)
        );
    }

    @Operation(
            summary = "메시지 전송 (REST 폴백)",
            description = """
                    WebSocket 연결이 불가능한 상황을 위한 폴백 전송 엔드포인트입니다.
                    실시간 연결이 정상일 때는 STOMP 발행(/pub/chat/rooms/{roomId}/send)을 사용합니다.

                    저장 후 구독자에게 브로드캐스트되며, clientMessageId 로 재전송 멱등 처리됩니다.
                    참여자만 전송할 수 있고, 전송 가능한 타입은 TEXT/IMAGE 입니다.
                    """
    )
    @PostMapping("/{roomId}/messages")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long roomId,
            @Valid @RequestBody ChatMessageSendRequest request
    ) {
        ChatMessageResponse response =
                chatMessageService.send(jwt.getSubject(), roomId, request);

        return ResponseEntity.ok(
                ApiResponse.success("메시지가 전송되었습니다.", response)
        );
    }

    @Operation(
            summary = "이미지 메시지 전송",
            description = """
                    이미지 메시지를 전송합니다. 첨부(URL·썸네일·크기 등)는 message_attachment로 저장됩니다.

                    파일 바이트는 서버가 다루지 않습니다. 클라이언트가 스토리지에 업로드한 뒤
                    확보한 URL을 attachments로 전달하세요. clientMessageId로 재전송이 멱등 처리됩니다.
                    """
    )
    @PostMapping("/{roomId}/messages/images")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendImageMessage(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long roomId,
            @Valid @RequestBody ImageMessageSendRequest request
    ) {
        ChatMessageResponse response =
                chatMessageService.sendImage(jwt.getSubject(), roomId, request);

        return ResponseEntity.ok(
                ApiResponse.success("이미지 메시지가 전송되었습니다.", response)
        );
    }

    @Operation(
            summary = "채팅방 나가기",
            description = """
                    채팅방에서 나갑니다(소프트). 메시지 이력은 보존되고 목록/상세에서 제외됩니다.

                    퇴장 SYSTEM 메시지가 발행되며, 방장이 나가면 남은 참여자 중 가장 먼저 입장한 사람에게
                    방장이 위임되고, 남은 참여자가 없으면 방이 종료됩니다.
                    """
    )
    @PostMapping("/{roomId}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveChatRoom(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long roomId
    ) {
        chatRoomService.leave(jwt.getSubject(), roomId);

        return ResponseEntity.ok(
                ApiResponse.success("채팅방에서 나갔습니다.")
        );
    }

    @Operation(
            summary = "채팅방 공지 등록",
            description = """
                    상단 고정 공지를 등록합니다. 방장(OWNER)만 등록할 수 있습니다.

                    기존 활성 공지가 있으면 해제되고 새 공지가 활성화됩니다(방당 활성 공지 1건).
                    등록 시 SYSTEM 메시지가 발행되어 채팅 흐름에도 표시됩니다.
                    """
    )
    @PostMapping("/{roomId}/notices")
    public ResponseEntity<ApiResponse<ChatNoticeResponse>> registerNotice(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long roomId,
            @Valid @RequestBody ChatNoticeCreateRequest request
    ) {
        ChatNoticeResponse response =
                chatNoticeService.register(jwt.getSubject(), roomId, request);

        return ResponseEntity.ok(
                ApiResponse.success("공지가 등록되었습니다.", response)
        );
    }

    @Operation(
            summary = "채팅방 활성 공지 조회",
            description = "현재 상단 고정 활성 공지를 조회합니다. 없으면 data=null. 참여자만 조회할 수 있습니다."
    )
    @GetMapping("/{roomId}/notices/active")
    public ResponseEntity<ApiResponse<ChatNoticeResponse>> getActiveNotice(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long roomId
    ) {
        ChatNoticeResponse response =
                chatNoticeService.getActive(jwt.getSubject(), roomId);

        return ResponseEntity.ok(
                ApiResponse.success("활성 공지 조회에 성공했습니다.", response)
        );
    }

    @Operation(
            summary = "채팅방 공지 해제",
            description = "현재 활성 공지를 해제합니다. 방장(OWNER)만 해제할 수 있습니다."
    )
    @DeleteMapping("/{roomId}/notices/active")
    public ResponseEntity<ApiResponse<Void>> deactivateNotice(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long roomId
    ) {
        chatNoticeService.deactivateActive(jwt.getSubject(), roomId);

        return ResponseEntity.ok(
                ApiResponse.success("공지가 해제되었습니다.")
        );
    }

    @Operation(
            summary = "메시지 읽음 처리",
            description = """
                    특정 메시지까지 읽음 처리하여 last_read_message_id 를 전진 갱신하고,
                    갱신 후의 안읽음 수(unreadCount)를 반환합니다.

                    참여 중인 사용자만 호출할 수 있습니다.
                    """
    )
    @PostMapping("/{roomId}/read")
    public ResponseEntity<ApiResponse<ChatReadResponse>> readMessages(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long roomId,
            @Valid @RequestBody ChatReadRequest request
    ) {
        ChatReadResponse response =
                chatRoomService.markRead(jwt.getSubject(), roomId, request);

        return ResponseEntity.ok(
                ApiResponse.success("읽음 처리되었습니다.", response)
        );
    }
}
