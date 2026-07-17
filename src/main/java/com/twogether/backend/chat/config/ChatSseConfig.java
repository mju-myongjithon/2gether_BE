package com.twogether.backend.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * SSE 하트비트(@Scheduled) 활성화. ChatUpdateStreamService 의 유휴 연결 유지를 위해 필요하다.
 */
@Configuration
@EnableScheduling
public class ChatSseConfig {
}
