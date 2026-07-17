package com.twogether.backend.notification.repository;

import com.twogether.backend.notification.domain.TelegramLinkCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TelegramLinkCodeRepository
        extends JpaRepository<TelegramLinkCode, Long> {

    Optional<TelegramLinkCode> findByCode(String code);
}
