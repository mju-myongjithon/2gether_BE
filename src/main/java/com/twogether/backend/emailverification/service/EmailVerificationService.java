package com.twogether.backend.emailverification.service;

import com.twogether.backend.emailverification.domain.EmailVerification;
import com.twogether.backend.emailverification.repository.EmailVerificationRepository;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.user.domain.User;
import com.twogether.backend.user.repository.UserRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@Transactional(readOnly = true)
public class EmailVerificationService {

    private static final Pattern SCHOOL_EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@mju\\.ac\\.kr$");

    private static final int CODE_EXPIRATION_MINUTES = 5;

    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final JavaMailSender mailSender;
    private final SecureRandom secureRandom = new SecureRandom();

    public EmailVerificationService(
            UserRepository userRepository,
            EmailVerificationRepository emailVerificationRepository,
            JavaMailSender mailSender
    ) {
        this.userRepository = userRepository;
        this.emailVerificationRepository = emailVerificationRepository;
        this.mailSender = mailSender;
    }

    @Transactional
    public void sendVerificationCode(
            String authUserId,
            String requestedEmail
    ) {
        User user = findUser(authUserId);
        String schoolEmail = normalizeEmail(requestedEmail);

        validateSchoolEmail(schoolEmail);

        if (user.isEmailVerified()) {
            throw new BusinessException(
                    ErrorCode.EMAIL_ALREADY_VERIFIED
            );
        }

        if (userRepository.existsBySchoolEmail(schoolEmail)) {
            throw new BusinessException(
                    ErrorCode.SCHOOL_EMAIL_ALREADY_IN_USE
            );
        }

        String verificationCode = generateVerificationCode();

        EmailVerification verification =
                new EmailVerification(
                        user.getId(),
                        schoolEmail,
                        verificationCode,
                        LocalDateTime.now()
                                .plusMinutes(CODE_EXPIRATION_MINUTES)
                );

        emailVerificationRepository.save(verification);

        sendEmail(schoolEmail, verificationCode);
    }

    @Transactional
    public void confirmVerificationCode(
            String authUserId,
            String requestedEmail,
            String requestedCode
    ) {
        User user = findUser(authUserId);
        String schoolEmail = normalizeEmail(requestedEmail);
        String verificationCode = normalizeCode(requestedCode);

        validateSchoolEmail(schoolEmail);

        if (user.isEmailVerified()) {
            throw new BusinessException(
                    ErrorCode.EMAIL_ALREADY_VERIFIED
            );
        }

        EmailVerification verification =
                emailVerificationRepository
                        .findTopByUserIdAndSchoolEmailOrderByCreatedAtDesc(
                                user.getId(),
                                schoolEmail
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.VERIFICATION_CODE_NOT_FOUND
                                )
                        );

        if (verification.isVerified()) {
            throw new BusinessException(
                    ErrorCode.ALREADY_USED_VERIFICATION_CODE
            );
        }

        if (verification.isExpired()) {
            throw new BusinessException(
                    ErrorCode.EXPIRED_VERIFICATION_CODE
            );
        }

        if (!verification.matches(verificationCode)) {
            throw new BusinessException(
                    ErrorCode.INVALID_VERIFICATION_CODE
            );
        }

        if (userRepository.existsBySchoolEmail(schoolEmail)) {
            throw new BusinessException(
                    ErrorCode.SCHOOL_EMAIL_ALREADY_IN_USE
            );
        }

        verification.verify();
        user.verifySchoolEmail(schoolEmail);
    }

    private User findUser(String authUserId) {
        return userRepository.findByAuthUserId(authUserId)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_SCHOOL_EMAIL
            );
        }

        return email.trim()
                .toLowerCase(Locale.ROOT);
    }

    private String normalizeCode(String code) {
        if (code == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_VERIFICATION_CODE
            );
        }

        return code.trim();
    }

    private void validateSchoolEmail(String email) {
        if (!SCHOOL_EMAIL_PATTERN.matcher(email).matches()) {
            throw new BusinessException(
                    ErrorCode.INVALID_SCHOOL_EMAIL
            );
        }
    }

    private String generateVerificationCode() {
        int code = secureRandom.nextInt(1_000_000);

        return String.format("%06d", code);
    }

    private void sendEmail(
            String schoolEmail,
            String verificationCode
    ) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(schoolEmail);
        message.setSubject("[2gether] 명지대학교 이메일 인증번호");
        message.setText(
                """
                2gether 학교 이메일 인증번호입니다.

                인증번호: %s

                인증번호는 5분 동안 유효합니다.
                본인이 요청하지 않았다면 이 메일을 무시해주세요.
                """.formatted(verificationCode)
        );

        mailSender.send(message);
    }
}