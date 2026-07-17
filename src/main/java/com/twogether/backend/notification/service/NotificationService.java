package com.twogether.backend.notification.service;

import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.global.response.PageResponse;
import com.twogether.backend.notification.domain.Notification;
import com.twogether.backend.notification.domain.NotificationType;
import com.twogether.backend.notification.dto.response.NotificationResponse;
import com.twogether.backend.notification.dto.response.NotificationUnreadCountResponse;
import com.twogether.backend.notification.repository.NotificationRepository;
import com.twogether.backend.user.domain.User;
import com.twogether.backend.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 인앱 알림함 서비스.
 *
 * 조회/읽음 처리 API를 제공하고, 다른 도메인 이벤트가 알림을 쌓을 수 있도록 저장 진입점({@link #create})을 노출한다.
 * (이벤트 구독→저장/전송 연결은 N3에서 이 서비스를 사용.)
 */
@Service
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    /**
     * 내 알림 목록(최신순, 페이징).
     */
    public PageResponse<NotificationResponse> getMyNotifications(
            String authUserId,
            int page,
            int size
    ) {
        User me = findUser(authUserId);

        Page<Notification> found = notificationRepository
                .findByUserIdOrderByIdDesc(me.getId(), PageRequest.of(page, size));

        List<NotificationResponse> content = found.getContent().stream()
                .map(NotificationResponse::from)
                .toList();

        return PageResponse.of(content, page, size, found.getTotalElements());
    }

    /**
     * 안읽은 알림 수(벨 배지).
     */
    public NotificationUnreadCountResponse getUnreadCount(
            String authUserId
    ) {
        User me = findUser(authUserId);
        long count = notificationRepository.countByUserIdAndReadAtIsNull(me.getId());
        return NotificationUnreadCountResponse.of(count);
    }

    /**
     * 단건 읽음 처리(본인 알림만). 이미 읽음이면 멱등.
     */
    @Transactional
    public void markAsRead(
            String authUserId,
            Long notificationId
    ) {
        User me = findUser(authUserId);

        Notification notification = notificationRepository
                .findByIdAndUserId(notificationId, me.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));

        notification.markRead();
    }

    /**
     * 내 미읽음 알림 전체 읽음 처리.
     */
    @Transactional
    public void markAllAsRead(
            String authUserId
    ) {
        User me = findUser(authUserId);

        notificationRepository.findAllByUserIdAndReadAtIsNull(me.getId())
                .forEach(Notification::markRead);
    }

    /**
     * 알림 저장 진입점(도메인 이벤트 → 알림 적재용, N3에서 사용).
     */
    @Transactional
    public Notification create(
            User recipient,
            NotificationType type,
            String title,
            String content,
            Map<String, Object> meta
    ) {
        return notificationRepository.save(
                Notification.of(recipient, type, title, content, meta)
        );
    }

    private User findUser(
            String authUserId
    ) {
        return userRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
