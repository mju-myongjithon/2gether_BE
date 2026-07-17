package com.twogether.backend.chat.controller;

import com.twogether.backend.chat.service.ChatUpdateStreamService;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.user.domain.User;
import com.twogether.backend.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(
        name = "채팅 실시간 갱신 API",
        description = "채팅방 목록 실시간 갱신용 SSE 구독 엔드포인트"
)
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/chat-rooms")
public class ChatUpdateSseController {

    private final ChatUpdateStreamService chatUpdateStreamService;
    private final UserRepository userRepository;

    public ChatUpdateSseController(
            ChatUpdateStreamService chatUpdateStreamService,
            UserRepository userRepository
    ) {
        this.chatUpdateStreamService = chatUpdateStreamService;
        this.userRepository = userRepository;
    }

    @Operation(
            summary = "채팅방 목록 갱신 SSE 구독",
            description = """
                    내가 참여 중인 방에 새 메시지가 도착하면 room-update 이벤트를 push 합니다.

                    EventSource 는 Authorization 헤더를 지원하지 않으므로
                    access_token 쿼리 파라미터로 JWT 를 전달할 수 있습니다(RFC 6750).
                    이벤트: connected(연결 확인), room-update(방 목록 갱신).
                    """
    )
    @GetMapping(value = "/updates", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal Jwt jwt) {
        User me = userRepository.findByAuthUserId(jwt.getSubject())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return chatUpdateStreamService.subscribe(me.getId());
    }
}
