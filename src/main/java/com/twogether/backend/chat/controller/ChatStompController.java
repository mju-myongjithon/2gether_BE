package com.twogether.backend.chat.controller;

import com.twogether.backend.chat.dto.request.ChatMessageSendRequest;
import com.twogether.backend.chat.service.ChatMessageService;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * 채팅 실시간 메시지 수신 처리.
 *
 * 클라이언트가 /pub/chat/rooms/{roomId}/send 로 발행하면 서버가 메시지를 저장하고,
 * /sub/chat/rooms/{roomId} 구독자에게 브로드캐스트한다(브로드캐스트는 서비스가 수행).
 * 발신자는 CONNECT 시 인증된 STOMP 세션 Principal(=auth_user_id)에서 추출한다.
 */
@Controller
public class ChatStompController {

    private final ChatMessageService chatMessageService;

    public ChatStompController(ChatMessageService chatMessageService) {
        this.chatMessageService = chatMessageService;
    }

    @MessageMapping("/chat/rooms/{roomId}/send")
    public void sendMessage(
            @DestinationVariable Long roomId,
            @Payload ChatMessageSendRequest request,
            Principal principal
    ) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        chatMessageService.send(principal.getName(), roomId, request);
    }
}
