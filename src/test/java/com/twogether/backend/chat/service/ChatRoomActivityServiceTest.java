package com.twogether.backend.chat.service;

import com.twogether.backend.chat.domain.ChatActivityStatus;
import com.twogether.backend.chat.dto.response.ChatRoomActivityResponse;
import com.twogether.backend.chat.repository.ChatRoomMemberRepository;
import com.twogether.backend.chat.repository.ChatRoomRepository;
import com.twogether.backend.chat.repository.MessageRepository;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.user.domain.User;
import com.twogether.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChatRoomActivityServiceTest {
    private final ChatRoomRepository roomRepository = mock(ChatRoomRepository.class);
    private final ChatRoomMemberRepository memberRepository = mock(ChatRoomMemberRepository.class);
    private final MessageRepository messageRepository = mock(MessageRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private ChatRoomActivityService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-07-17T03:00:00Z"), ZoneOffset.UTC);
        service = new ChatRoomActivityService(roomRepository, memberRepository, messageRepository, userRepository, clock);
    }

    @ParameterizedTest
    @CsvSource({"0,INACTIVE", "1,LOW", "9,LOW", "10,MEDIUM", "29,MEDIUM", "30,HIGH"})
    void activityStatusBoundaries(long count, ChatActivityStatus expected) {
        assertThat(ChatRoomActivityService.status(count)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"0,0", "1,1", "4,1", "5,2", "9,2", "10,3", "19,3", "20,4"})
    void levelBoundaries(long count, int expected) {
        assertThat(ChatRoomActivityService.level(count)).isEqualTo(expected);
    }

    @Test
    void returnsThirtyDaysIncludingEmptyDaysAndGroupsBySeoulDate() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(7L);
        when(userRepository.findByAuthUserId("subject")).thenReturn(Optional.of(user));
        when(roomRepository.existsById(1L)).thenReturn(true);
        when(memberRepository.existsByChatRoomIdAndUserIdAndLeftAtIsNull(1L, 7L)).thenReturn(true);
        when(messageRepository.countActivityMessages(eq(1L), any(), any())).thenReturn(10L);
        when(messageRepository.findActivityMessageCreatedAt(eq(1L), any(), any())).thenReturn(List.of(
                OffsetDateTime.parse("2026-07-16T15:00:00Z"),
                OffsetDateTime.parse("2026-07-17T14:59:59Z")
        ));

        ChatRoomActivityResponse result = service.getActivity("subject", 1L);

        assertThat(result.startDate().toString()).isEqualTo("2026-06-18");
        assertThat(result.endDate().toString()).isEqualTo("2026-07-17");
        assertThat(result.dailyActivities()).hasSize(30);
        assertThat(result.dailyActivities().get(0).messageCount()).isZero();
        assertThat(result.dailyActivities().get(29).messageCount()).isEqualTo(2);
        assertThat(result.activityStatus()).isEqualTo(ChatActivityStatus.MEDIUM);
    }

    @Test
    void rejectsNonMember() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(7L);
        when(userRepository.findByAuthUserId("subject")).thenReturn(Optional.of(user));
        when(roomRepository.existsById(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.getActivity("subject", 1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);
    }
}
