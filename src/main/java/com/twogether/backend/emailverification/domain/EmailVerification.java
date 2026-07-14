package com.twogether.backend.emailverification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_verification")
public class EmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "user_id",
            nullable = false
    )
    private Long userId;

    @Column(
            name = "school_email",
            nullable = false
    )
    private String schoolEmail;

    @Column(
            name = "verification_code",
            nullable = false,
            length = 6
    )
    private String verificationCode;

    @Column(
            name = "expires_at",
            nullable = false
    )
    private LocalDateTime expiresAt;

    @Column(
            name = "verified",
            nullable = false
    )
    private boolean verified;

    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDateTime createdAt;

    protected EmailVerification() {
    }

    public EmailVerification(
            Long userId,
            String schoolEmail,
            String verificationCode,
            LocalDateTime expiresAt
    ) {
        this.userId = userId;
        this.schoolEmail = schoolEmail;
        this.verificationCode = verificationCode;
        this.expiresAt = expiresAt;
        this.verified = false;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getSchoolEmail() {
        return schoolEmail;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public boolean isVerified() {
        return verified;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean matches(String code) {
        return verificationCode.equals(code);
    }

    public void verify() {
        this.verified = true;
    }
}