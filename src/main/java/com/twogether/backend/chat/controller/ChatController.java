package com.twogether.backend.chat.controller;

import com.twogether.backend.chat.domain.ChatRoomType;
import com.twogether.backend.chat.domain.MessageType;
import com.twogether.backend.chat.dto.request.ChatMessageSendRequest;
import com.twogether.backend.chat.dto.request.ChatReadRequest;
import com.twogether.backend.chat.dto.response.ChatMessageResponse;
import com.twogether.backend.chat.dto.response.ChatReadResponse;
import com.twogether.backend.chat.dto.response.ChatRoomDetailResponse;
import com.twogether.backend.chat.dto.response.ChatRoomMemberResponse;
import com.twogether.backend.chat.dto.response.ChatRoomSummaryResponse;
import com.twogether.backend.gatheringmember.domain.GatheringMemberRole;
import com.twogether.backend.global.response.ApiResponse;
import com.twogether.backend.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
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
        description = "그룹 채팅방 조회, 메시지 이력 조회, 메시지 전송(REST 폴백), 읽음 처리 관련 API"
)
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Operation(
            summary = "내 채팅방 목록 조회",
            description = """
                    내가 참여 중인 그룹 채팅방 목록을 최근 메시지 순으로 조회합니다.

                    페이지네이션은 page=0, size=20 방식을 기본으로 합니다.

                    현재 Swagger 명세 단계에서는 더미 채팅방 목록을 반환합니다.
                    """
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<PageResponse<ChatRoomSummaryResponse>>> getChatRooms(
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size
    ) {
        ChatRoomSummaryResponse summary = new ChatRoomSummaryResponse(
                10L,
                1L,
                ChatRoomType.GROUP,
                "인문X자연 해커톤 팀",
                4,
                "내일 7시에 봬요!",
                OffsetDateTime.parse("2026-07-13T21:10:00+09:00"),
                2
        );

        PageResponse<ChatRoomSummaryResponse> response =
                PageResponse.of(List.of(summary), page, size, 1);

        return ResponseEntity.ok(
                ApiResponse.success("채팅방 목록 조회에 성공했습니다.", response)
        );
    }

    @Operation(
            summary = "채팅방 상세 조회",
            description = """
                    채팅방 기본 정보와 참여자 목록을 조회합니다.

                    현재 Swagger 명세 단계에서는 더미 채팅방 상세 정보를 반환합니다.
                    """
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ApiResponse<ChatRoomDetailResponse>> getChatRoom(
            @PathVariable Long roomId
    ) {
        List<ChatRoomMemberResponse> members = List.of(
                new ChatRoomMemberResponse(1L, "인준", "컴퓨터공학과", "NATURAL", GatheringMemberRole.HOST),
                new ChatRoomMemberResponse(2L, "기획러", "경영학과", "HUMANITIES", GatheringMemberRole.MEMBER)
        );

        ChatRoomDetailResponse response = new ChatRoomDetailResponse(
                roomId,
                1L,
                ChatRoomType.GROUP,
                "인문X자연 해커톤 팀",
                OffsetDateTime.parse("2026-07-09T20:20:00+09:00"),
                members
        );

        return ResponseEntity.ok(
                ApiResponse.success("채팅방 조회에 성공했습니다.", response)
        );
    }

    @Operation(
            summary = "채팅방 메시지 이력 조회",
            description = """
                    채팅방의 이전 메시지를 페이지네이션으로 조회합니다. (최신 → 과거 순)

                    SYSTEM 메시지는 senderId, senderNickname이 null입니다.

                    현재 Swagger 명세 단계에서는 더미 메시지 목록을 반환합니다.
                    """
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/rooms/{roomId}/messages")
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

                    실시간 연결이 정상일 때는 WebSocket(STOMP) 발행을 사용합니다.

                    현재 Swagger 명세 단계에서는 실제 저장 없이 더미 응답을 반환합니다.
                    """
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/rooms/{roomId}/messages")
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

                    현재 Swagger 명세 단계에서는 실제 반영 없이 더미 응답을 반환합니다.
                    """
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/rooms/{roomId}/read")
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
