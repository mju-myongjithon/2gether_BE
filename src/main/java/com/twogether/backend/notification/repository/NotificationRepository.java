package com.twogether.backend.notification.repository;

import com.twogether.backend.notification.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    /*
     * 내 알림 목록(최신순).
     */
    Page<Notification> findByUserIdOrderByIdDesc(
            Long userId,
            Pageable pageable
    );

    /*
     * 안읽음 수(벨 배지).
     */
    long countByUserIdAndReadAtIsNull(
            Long userId
    );

    /*
     * 소유권 검증 포함 단건 조회.
     */
    Optional<Notification> findByIdAndUserId(
            Long id,
            Long userId
    );

    /*
     * 전체 읽음 처리용 미읽음 목록.
     */
    List<Notification> findAllByUserIdAndReadAtIsNull(
            Long userId
    );
}
