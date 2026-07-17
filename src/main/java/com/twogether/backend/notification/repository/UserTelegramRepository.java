package com.twogether.backend.notification.repository;

import com.twogether.backend.notification.domain.UserTelegram;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTelegramRepository
        extends JpaRepository<UserTelegram, Long> {
    // PK == user_id 이므로 findById(userId)/existsById/deleteById 사용
}
