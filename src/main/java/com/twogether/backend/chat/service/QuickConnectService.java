package com.twogether.backend.chat.service;

import com.twogether.backend.chat.domain.ChatRoom;
import com.twogether.backend.chat.domain.ChatRoomMember;
import com.twogether.backend.chat.domain.Message;
import com.twogether.backend.chat.domain.QuickCodeStatus;
import com.twogether.backend.chat.domain.QuickConnectCode;
import com.twogether.backend.chat.dto.response.ChatMessageResponse;
import com.twogether.backend.chat.dto.response.QuickConnectCreateResponse;
import com.twogether.backend.chat.dto.response.QuickConnectJoinResponse;
import com.twogether.backend.chat.repository.ChatRoomMemberRepository;
import com.twogether.backend.chat.repository.ChatRoomRepository;
import com.twogether.backend.chat.repository.MessageRepository;
import com.twogether.backend.chat.repository.QuickConnectCodeRepository;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.user.domain.User;
import com.twogether.backend.user.repository.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * 안심 커넥트(Quick Connect) 서비스.
 *
 * - 생성: QUICK_CONNECT 방 + 6자리 코드 발급(만료 시각 포함). 생성자는 OWNER 로 입장.
 * - 입장: 코드 검증(ACTIVE/만료/사용) 후 참여자 등록, 코드는 일회성으로 USED 처리.
 *
 * 활성 코드 유일성은 앱 레벨(생성 시 중복 확인)로 보장한다. DB 강제(부분 유니크 WHERE status=ACTIVE)는
 * ddl-auto=update 로는 생성되지 않으므로 마이그레이션에서 추가한다.
 * 메시지 자동삭제(종료 시)·코드 TTL 관리는 후속(Redis TTL) 논의 대상.
 */
@Service
@Transactional(readOnly = true)
public class QuickConnectService {

    private static final String BROADCAST_DESTINATION_PREFIX = "/sub/chat/rooms/";
    private static final String ROOM_TITLE = "빠른 연결";
    private static final Duration CODE_TTL = Duration.ofHours(24);
    private static final int CODE_BOUND = 1_000_000;
    private static final int MAX_GENERATE_ATTEMPTS = 10;

    private final QuickConnectCodeRepository quickConnectCodeRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    public QuickConnectService(
            QuickConnectCodeRepository quickConnectCodeRepository,
            ChatRoomRepository chatRoomRepository,
            ChatRoomMemberRepository chatRoomMemberRepository,
            MessageRepository messageRepository,
            UserRepository userRepository,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.quickConnectCodeRepository = quickConnectCodeRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.chatRoomMemberRepository = chatRoomMemberRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * 안심 커넥트 방과 6자리 코드를 생성한다. 생성자는 OWNER 로 등록된다.
     */
    @Transactional
    public QuickConnectCreateResponse create(
            String authUserId
    ) {
        User creator = findUser(authUserId);

        ChatRoom room = chatRoomRepository.save(ChatRoom.quickConnect(ROOM_TITLE));
        chatRoomMemberRepository.save(ChatRoomMember.owner(room, creator));

        String code = generateUniqueCode();
        OffsetDateTime expiresAt = OffsetDateTime.now().plus(CODE_TTL);

        quickConnectCodeRepository.save(
                new QuickConnectCode(room, creator, code, expiresAt)
        );

        return new QuickConnectCreateResponse(room.getId(), code, expiresAt);
    }

    /**
     * 코드로 방에 입장한다. 코드가 유효하지 않거나 만료/사용된 경우 각각 예외를 던진다.
     * 성공 시 참여자로 등록하고 코드를 일회성으로 USED 처리한다.
     */
    @Transactional
    public QuickConnectJoinResponse join(
            String authUserId,
            String code
    ) {
        User user = findUser(authUserId);

        QuickConnectCode quickCode = quickConnectCodeRepository
                .findByCodeAndStatus(code, QuickCodeStatus.ACTIVE)
                .orElseThrow(() -> resolveInvalidCode(code));

        if (quickCode.isExpired(OffsetDateTime.now())) {
            quickCode.markExpired();
            throw new BusinessException(ErrorCode.EXPIRED_QUICK_CODE);
        }

        ChatRoom room = quickCode.getChatRoom();

        Optional<ChatRoomMember> existing = chatRoomMemberRepository
                .findByChatRoomIdAndUserId(room.getId(), user.getId());
        if (existing.isPresent()) {
            // 이미 방에 속해 있으면(생성자/재시도) 코드 소비 없이 방 ID만 반환
            ChatRoomMember membership = existing.get();
            if (!membership.isParticipating()) {
                membership.rejoin();
            }
            return new QuickConnectJoinResponse(room.getId());
        }

        chatRoomMemberRepository.save(ChatRoomMember.member(room, user));
        quickCode.markUsed();

        publishJoinSystemMessage(room, user);

        return new QuickConnectJoinResponse(room.getId());
    }

    private BusinessException resolveInvalidCode(
            String code
    ) {
        return quickConnectCodeRepository.findFirstByCodeOrderByCreatedAtDesc(code)
                .map(existing -> switch (existing.getStatus()) {
                    case USED -> new BusinessException(ErrorCode.ALREADY_USED_QUICK_CODE);
                    case EXPIRED -> new BusinessException(ErrorCode.EXPIRED_QUICK_CODE);
                    case ACTIVE -> new BusinessException(ErrorCode.INVALID_QUICK_CODE);
                })
                .orElseGet(() -> new BusinessException(ErrorCode.INVALID_QUICK_CODE));
    }

    private String generateUniqueCode() {
        for (int attempt = 0; attempt < MAX_GENERATE_ATTEMPTS; attempt++) {
            String candidate = String.format("%06d", secureRandom.nextInt(CODE_BOUND));
            boolean activeExists = quickConnectCodeRepository
                    .existsByCodeAndStatus(candidate, QuickCodeStatus.ACTIVE);
            if (!activeExists) {
                return candidate;
            }
        }
        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private void publishJoinSystemMessage(
            ChatRoom room,
            User user
    ) {
        Message systemMessage = messageRepository.save(
                Message.system(
                        room,
                        user.getNickname() + "님이 입장했습니다.",
                        Map.of("systemType", "MEMBER_JOINED", "userId", user.getId())
                )
        );
        room.updateLastMessage(systemMessage.getId(), systemMessage.getCreatedAt());
        messagingTemplate.convertAndSend(
                BROADCAST_DESTINATION_PREFIX + room.getId(),
                ChatMessageResponse.from(systemMessage)
        );
    }

    private User findUser(
            String authUserId
    ) {
        return userRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
