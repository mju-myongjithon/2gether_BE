package com.twogether.backend.chat.controller;

import com.twogether.backend.chat.domain.MessageType;
import com.twogether.backend.chat.dto.request.ChatMessageSendRequest;
import com.twogether.backend.chat.dto.request.ChatReadRequest;
import com.twogether.backend.chat.dto.response.ChatMessageResponse;
import com.twogether.backend.chat.dto.response.ChatReadResponse;
import com.twogether.backend.chat.dto.response.ChatRoomDetailResponse;
import com.twogether.backend.chat.dto.response.ChatRoomSummaryResponse;
import com.twogether.backend.chat.service.ChatRoomService;
import com.twogether.backend.global.response.ApiResponse;
import com.twogether.backend.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.time.OffsetDateTime;
import java.util.List;

@Tag(
        name = "채팅 API",
        description = "채팅방 목록/상세 조회, 메시지 이력 조회, 메시지 전송(REST 폴백), 읽음 처리 관련 API"
)
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/chat-rooms")
public class ChatController {

    private final ChatRoomService chatRoomService;

    public ChatController(
            ChatRoomService chatRoomService
    ) {
        this.chatRoomService = chatRoomService;
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
            summary = "채팅방 메시지 이력 조회",
            description = """
                    채팅방의 이전 메시지를 조회합니다. (최신 → 과거 순)

                    ⚠️ 현재는 스켈레톤 더미 응답입니다. 실제 커서 페이징 구현은 이슈 C4에서 진행합니다.
                    """
    )
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<ApiResponse<PageResponse<ChatMessageResponse>>> getMessages(
            @PathVariable Long roomId,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size
    ) {
        ChatMessageResponse textMessage = new ChatMessageResponse(
                105L,
                roomId,
                1L,
                "인준",
                MessageType.TEXT,
                "내일 7시에 봬요!",
                OffsetDateTime.parse("2026-07-13T21:10:00+09:00")
        );

        ChatMessageResponse systemMessage = new ChatMessageResponse(
                100L,
                roomId,
                null,
                null,
                MessageType.SYSTEM,
                "기획러님이 입장했습니다.",
                OffsetDateTime.parse("2026-07-09T20:21:00+09:00")
        );

        PageResponse<ChatMessageResponse> response =
                PageResponse.of(List.of(textMessage, systemMessage), page, size, 2);

        return ResponseEntity.ok(
                ApiResponse.success("메시지 조회에 성공했습니다.", response)
        );
    }

    @Operation(
            summary = "메시지 전송 (REST 폴백)",
            description = """
                    WebSocket 연결이 불가능한 상황을 위한 폴백 전송 엔드포인트입니다.

                    ⚠️ 현재는 스켈레톤 더미 응답입니다. 실제 저장/브로드캐스트는 이슈 C4에서 진행합니다.
                    """
    )
    @PostMapping("/{roomId}/messages")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(
            @PathVariable Long roomId,
            @RequestBody ChatMessageSendRequest request
    ) {
        ChatMessageResponse response = new ChatMessageResponse(
                106L,
                roomId,
                1L,
                "인준",
                request.type(),
                request.content(),
                OffsetDateTime.parse("2026-07-14T10:00:00+09:00")
        );

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
