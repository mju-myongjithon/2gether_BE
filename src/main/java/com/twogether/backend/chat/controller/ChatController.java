package com.twogether.backend.chat.controller;

import com.twogether.backend.chat.dto.request.ChatMessageSendRequest;
import com.twogether.backend.chat.dto.request.ChatReadRequest;
import com.twogether.backend.chat.dto.response.ChatMessagePageResponse;
import com.twogether.backend.chat.dto.response.ChatMessageResponse;
import com.twogether.backend.chat.dto.response.ChatReadResponse;
import com.twogether.backend.chat.dto.response.ChatRoomDetailResponse;
import com.twogether.backend.chat.dto.response.ChatRoomSummaryResponse;
import com.twogether.backend.chat.service.ChatMessageService;
import com.twogether.backend.chat.service.ChatRoomService;
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

    public ChatController(
            ChatRoomService chatRoomService,
            ChatMessageService chatMessageService
    ) {
        this.chatRoomService = chatRoomService;
        this.chatMessageService = chatMessageService;
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
            summary = "메시지 읽음 처리",
            description = """
                    특정 메시지까지 읽음 처리하여 unreadCount를 갱신합니다.

                    ⚠️ 현재는 스켈레톤 더미 응답입니다. 실제 반영은 이슈 C5에서 진행합니다.
                    """
    )
    @PostMapping("/{roomId}/read")
    public ResponseEntity<ApiResponse<ChatReadResponse>> readMessages(
            @PathVariable Long roomId,
            @RequestBody ChatReadRequest request
    ) {
        ChatReadResponse response = new ChatReadResponse(
                roomId,
                request.lastReadMessageId(),
                0
        );

        return ResponseEntity.ok(
                ApiResponse.success("읽음 처리되었습니다.", response)
        );
    }
}
