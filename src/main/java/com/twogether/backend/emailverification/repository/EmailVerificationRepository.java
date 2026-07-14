package com.twogether.backend.emailverification.repository;

import com.twogether.backend.emailverification.domain.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationRepository
        extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findTopByUserIdAndSchoolEmailOrderByCreatedAtDesc(
            Long userId,
            String schoolEmail
    );
}