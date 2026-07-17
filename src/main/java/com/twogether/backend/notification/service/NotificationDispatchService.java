package com.twogether.backend.notification.service;

import com.twogether.backend.notification.domain.Notification;
import com.twogether.backend.notification.domain.NotificationType;
import com.twogether.backend.notification.dto.response.NotificationPushMessage;
import com.twogether.backend.notification.repository.UserTelegramRepository;
import com.twogether.backend.notification.telegram.TelegramSender;
import com.twogether.backend.user.domain.User;
import com.twogether.backend.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 알림 발송 디스패처.
 *
 * - 이벤트성({@link #notifyEvent}): notification 저장 + 인앱 WS 푸시 + (연결 시)텔레그램.
 * - 새 채팅 메시지({@link #notifyChatMessage}): 저장 + 푸시 + 텔레그램.
 *
 * 인앱 푸시 경로: /sub/users/{userId}/notifications (기존 SimpleBroker "/sub" prefix 활용).
 * 각 채널은 격리(try/catch)되어 하나가 실패해도 나머지에 영향 없음.
 */
@Service
public class NotificationDispatchService {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatchService.class);
    private static final String PUSH_DESTINATION_PREFIX = "/sub/users/";
    private static final String PUSH_DESTINATION_SUFFIX = "/notifications";

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final UserTelegramRepository userTelegramRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final TelegramSender telegramSender;

    public NotificationDispatchService(
            NotificationService notificationService,
            UserRepository userRepository,
            UserTelegramRepository userTelegramRepository,
            SimpMessagingTemplate messagingTemplate,
            TelegramSender telegramSender
    ) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.userTelegramRepository = userTelegramRepository;
        this.messagingTemplate = messagingTemplate;
        this.telegramSender = telegramSender;
    }

    /**
     * 이벤트성 알림: 저장 + 인앱 푸시 + 텔레그램.
     */
    public void notifyEvent(
            Long recipientUserId,
            NotificationType type,
            String title,
            String content,
            Map<String, Object> meta
    ) {
        User recipient = userRepository.findById(recipientUserId).orElse(null);
        if (recipient == null) {
            log.warn("알림 대상 사용자 없음 userId={}", recipientUserId);
            return;
        }

        Notification saved = notificationService.create(recipient, type, title, content, meta);

        pushInApp(
                recipientUserId,
                NotificationPushMessage.notification(type, title, content, meta, saved.getCreatedAt())
        );
        sendTelegram(recipientUserId, buildTelegramText(title, content));
    }

    /**
     * 새 채팅 메시지 알림: 저장 + 인앱 푸시 + 텔레그램.
     */
    public void notifyChatMessage(
            Long recipientUserId,
            Long roomId,
            String text
    ) {
        User recipient = userRepository.findById(recipientUserId).orElse(null);
        if (recipient == null) {
            log.warn("알림 대상 사용자 없음 userId={}", recipientUserId);
            return;
        }

        Notification saved = notificationService.create(
                recipient,
                NotificationType.CHAT_MESSAGE,
                "새 채팅 알림",
                text,
                Map.of("roomId", roomId)
        );

        pushInApp(
                recipientUserId,
                NotificationPushMessage.notification(
                        NotificationType.CHAT_MESSAGE,
                        saved.getTitle(),
                        saved.getContent(),
                        saved.getMeta(),
                        saved.getCreatedAt()
                )
        );
        sendTelegram(recipientUserId, text);
    }

    private void pushInApp(
            Long userId,
            NotificationPushMessage payload
    ) {
        try {
            messagingTemplate.convertAndSend(
                    PUSH_DESTINATION_PREFIX + userId + PUSH_DESTINATION_SUFFIX,
                    payload
            );
        } catch (Exception e) {
            log.warn("인앱 알림 푸시 실패 userId={}: {}", userId, e.getMessage());
        }
    }

    private void sendTelegram(
            Long userId,
            String text
    ) {
        userTelegramRepository.findById(userId)
                .ifPresent(userTelegram -> telegramSender.send(userTelegram.getTelegramChatId(), text));
    }

    private String buildTelegramText(
            String title,
            String content
    ) {
        if (content == null || content.isBlank()) {
            return title;
        }
        return title + "\n" + content;
    }
}
