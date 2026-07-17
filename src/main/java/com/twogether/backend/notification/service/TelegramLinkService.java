package com.twogether.backend.notification.service;

import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.notification.config.TelegramProperties;
import com.twogether.backend.notification.domain.TelegramLinkCode;
import com.twogether.backend.notification.domain.UserTelegram;
import com.twogether.backend.notification.dto.response.TelegramLinkResponse;
import com.twogether.backend.notification.dto.response.TelegramStatusResponse;
import com.twogether.backend.notification.repository.TelegramLinkCodeRepository;
import com.twogether.backend.notification.repository.UserTelegramRepository;
import com.twogether.backend.user.domain.User;
import com.twogether.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 텔레그램 연결 서비스.
 *
 * - 연결: 딥링크 코드 발급 → 사용자가 봇 /start {code} → 웹훅 수신 시 chat_id 저장.
 * - 조회/해제: 내 연결 상태, 연결 해제.
 *
 * 수신 방식(웹훅)과 무관하게 {@link #handleStartCommand} 가 실제 연결 처리를 담당하므로,
 * 추후 폴링을 붙여도 이 메서드를 재사용한다.
 */
@Service
@Transactional(readOnly = true)
public class TelegramLinkService {

    private static final Duration LINK_CODE_TTL = Duration.ofMinutes(10);
    private static final String START_COMMAND = "/start";

    private final UserTelegramRepository userTelegramRepository;
    private final TelegramLinkCodeRepository telegramLinkCodeRepository;
    private final UserRepository userRepository;
    private final TelegramProperties telegramProperties;

    public TelegramLinkService(
            UserTelegramRepository userTelegramRepository,
            TelegramLinkCodeRepository telegramLinkCodeRepository,
            UserRepository userRepository,
            TelegramProperties telegramProperties
    ) {
        this.userTelegramRepository = userTelegramRepository;
        this.telegramLinkCodeRepository = telegramLinkCodeRepository;
        this.userRepository = userRepository;
        this.telegramProperties = telegramProperties;
    }

    /**
     * 봇 연결용 딥링크와 일회성 코드를 발급한다.
     */
    @Transactional
    public TelegramLinkResponse issueLinkCode(
            String authUserId
    ) {
        User me = findUser(authUserId);

        String code = UUID.randomUUID().toString().replace("-", "");
        OffsetDateTime expiresAt = OffsetDateTime.now().plus(LINK_CODE_TTL);

        telegramLinkCodeRepository.save(
                new TelegramLinkCode(me.getId(), code, expiresAt)
        );

        String deepLink = "https://t.me/" + telegramProperties.getBotUsername() + "?start=" + code;
        return new TelegramLinkResponse(deepLink, code, expiresAt);
    }

    /**
     * 내 텔레그램 연결 상태.
     */
    public TelegramStatusResponse getStatus(
            String authUserId
    ) {
        User me = findUser(authUserId);

        return userTelegramRepository.findById(me.getId())
                .map(ut -> TelegramStatusResponse.linked(ut.getTelegramChatId(), ut.getLinkedAt()))
                .orElseGet(TelegramStatusResponse::notLinked);
    }

    /**
     * 텔레그램 연결 해제(멱등). 미연결이어도 성공 처리한다.
     */
    @Transactional
    public void unlink(
            String authUserId
    ) {
        User me = findUser(authUserId);
        userTelegramRepository.findById(me.getId())
                .ifPresent(userTelegramRepository::delete);
    }

    /**
     * 봇 /start {code} 수신 처리. 코드가 유효하면 사용자↔chat_id 를 연결하고 코드를 소비한다.
     * 유효하지 않으면 조용히 무시한다(재전송/스팸 안전).
     */
    @Transactional
    public void handleStartCommand(
            Long chatId,
            String text
    ) {
        String code = parseStartCode(text);
        if (code == null) {
            return;
        }

        TelegramLinkCode linkCode = telegramLinkCodeRepository.findByCode(code).orElse(null);
        if (linkCode == null || !linkCode.isUsable(OffsetDateTime.now())) {
            return;
        }

        Long userId = linkCode.getUserId();
        userTelegramRepository.findById(userId)
                .ifPresentOrElse(
                        existing -> existing.relink(chatId),
                        () -> userTelegramRepository.save(new UserTelegram(userId, chatId))
                );

        linkCode.markUsed();
    }

    private String parseStartCode(
            String text
    ) {
        if (text == null || !text.startsWith(START_COMMAND)) {
            return null;
        }
        String[] parts = text.trim().split("\\s+");
        if (parts.length < 2 || parts[1].isBlank()) {
            return null;
        }
        return parts[1];
    }

    private User findUser(
            String authUserId
    ) {
        return userRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
