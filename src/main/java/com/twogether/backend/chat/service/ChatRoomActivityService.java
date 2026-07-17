package com.twogether.backend.chat.service;

import com.twogether.backend.chat.domain.ChatActivityStatus;
import com.twogether.backend.chat.dto.response.ChatDailyActivityResponse;
import com.twogether.backend.chat.dto.response.ChatRoomActivityResponse;
import com.twogether.backend.chat.repository.ChatRoomMemberRepository;
import com.twogether.backend.chat.repository.ChatRoomRepository;
import com.twogether.backend.chat.repository.MessageRepository;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.user.domain.User;
import com.twogether.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Service
@Transactional(readOnly = true)
public class ChatRoomActivityService {
    static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    public ChatRoomActivityService(ChatRoomRepository chatRoomRepository,
                                   ChatRoomMemberRepository chatRoomMemberRepository,
                                   MessageRepository messageRepository,
                                   UserRepository userRepository,
                                   Clock clock) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatRoomMemberRepository = chatRoomMemberRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    public ChatRoomActivityResponse getActivity(String authUserId, Long chatRoomId) {
        User user = userRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (!chatRoomRepository.existsById(chatRoomId)) {
            throw new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND);
        }
        if (!chatRoomMemberRepository.existsByChatRoomIdAndUserIdAndLeftAtIsNull(chatRoomId, user.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        LocalDate endDate = LocalDate.now(clock.withZone(SEOUL));
        LocalDate startDate = endDate.minusDays(29);
        OffsetDateTime periodStart = startDate.atStartOfDay(SEOUL).toOffsetDateTime();
        OffsetDateTime periodEnd = endDate.plusDays(1).atStartOfDay(SEOUL).toOffsetDateTime();
        OffsetDateTime sevenDayStart = endDate.minusDays(6).atStartOfDay(SEOUL).toOffsetDateTime();

        long recentCount = messageRepository.countActivityMessages(chatRoomId, sevenDayStart, periodEnd);
        Map<LocalDate, Long> dailyCounts = messageRepository
                .findActivityMessageCreatedAt(chatRoomId, periodStart, periodEnd)
                .stream()
                .collect(Collectors.groupingBy(
                        createdAt -> createdAt.atZoneSameInstant(SEOUL).toLocalDate(),
                        Collectors.counting()
                ));

        List<ChatDailyActivityResponse> activities = LongStream.range(0, 30)
                .mapToObj(startDate::plusDays)
                .map(date -> {
                    long count = dailyCounts.getOrDefault(date, 0L);
                    return new ChatDailyActivityResponse(date, count, level(count));
                })
                .toList();

        return new ChatRoomActivityResponse(chatRoomId, recentCount, status(recentCount),
                startDate, endDate, activities);
    }

    static ChatActivityStatus status(long count) {
        if (count == 0) return ChatActivityStatus.INACTIVE;
        if (count <= 9) return ChatActivityStatus.LOW;
        if (count <= 29) return ChatActivityStatus.MEDIUM;
        return ChatActivityStatus.HIGH;
    }

    static int level(long count) {
        if (count == 0) return 0;
        if (count <= 4) return 1;
        if (count <= 9) return 2;
        if (count <= 19) return 3;
        return 4;
    }
}
