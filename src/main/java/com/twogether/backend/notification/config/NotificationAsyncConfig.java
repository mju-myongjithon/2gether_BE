package com.twogether.backend.notification.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 알림 리스너 비동기 실행 활성화. 채팅/모임 코어 트랜잭션과 분리해 발송 지연·장애가 전파되지 않도록 한다.
 */
@Configuration
@EnableAsync
public class NotificationAsyncConfig {
}
