package com.twogether.backend.chat.controller;

import com.twogether.backend.chat.dto.request.ChatMessageSendRequest;
import com.twogether.backend.chat.dto.response.ChatMessageResponse;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.OffsetDateTime;

/**
 * 채팅 실시간 메시지 발행/브로드캐스트 처리.
 *
 * 클라이언트가 /pub/chat/rooms/{roomId}/send 로 메시지를 발행하면,
 * 서버가 /sub/chat/rooms/{roomId} 구독자에게 브로드캐스트합니다.
 *
 * 현재 명세 단계에서는 실제 저장 없이 수신 페이로드를 그대로 브로드캐스트합니다.
 * 발신자(senderId)는 추후 STOMP 세션의 인증 정보에서 추출합니다.
 */
@Controller
public class ChatStompController {

    private final SimpMessagingTemplate messagingTemplate;

    public ChatStompController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat/rooms/{roomId}/send")
    public void sendMessage(
            @DestinationVariable Long roomId,
            ChatMessageSendRequest request
    ) {
        ChatMessageResponse response = new ChatMessageResponse(
                107L,
                roomId,
                1L,
                "인준",
                request.type(),
                request.content(),
                OffsetDateTime.now()
        );

        messagingTemplate.convertAndSend("/sub/chat/rooms/" + roomId, response);
    }
}
