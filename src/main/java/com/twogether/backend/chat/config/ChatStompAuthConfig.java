package com.twogether.backend.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;

/**
 * STOMP 연결(CONNECT) 시 Authorization 헤더의 JWT 를 검증해 세션 사용자를 설정한다.
 *
 * 기존 {@code global/config/WebSocketConfig} 를 수정하지 않고, chat 패키지에서
 * 추가 {@link WebSocketMessageBrokerConfigurer} 로 인바운드 채널 인터셉터만 얹는다.
 * 이후 {@code @MessageMapping} 에서 {@link Principal#getName()} = auth_user_id 로 발신자를 식별한다.
 */
@Configuration
public class ChatStompAuthConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtDecoder jwtDecoder;

    public ChatStompAuthConfig(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authorization = accessor.getFirstNativeHeader("Authorization");
                    if (authorization != null && authorization.startsWith("Bearer ")) {
                        Jwt jwt = jwtDecoder.decode(authorization.substring(7));
                        accessor.setUser(new StompPrincipal(jwt.getSubject()));
                    }
                }
                return message;
            }
        });
    }

    /**
     * STOMP 세션 사용자. name = auth_user_id(Supabase sub).
     */
    private record StompPrincipal(String name) implements Principal {
        @Override
        public String getName() {
            return name;
        }
    }
}
