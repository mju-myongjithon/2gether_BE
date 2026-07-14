package com.twogether.backend.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * 채팅 실시간 통신을 위한 STOMP over WebSocket 설정.
 *
 * - 연결 엔드포인트: /ws-stomp (SockJS 지원)
 * - 구독(SUBSCRIBE): /sub/chat/rooms/{roomId}
 * - 발행(SEND):      /pub/chat/rooms/{roomId}/send
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트 구독 prefix
        registry.enableSimpleBroker("/sub");
        // 클라이언트 발행 prefix (@MessageMapping 라우팅)
        registry.setApplicationDestinationPrefixes("/pub");
    }
}
