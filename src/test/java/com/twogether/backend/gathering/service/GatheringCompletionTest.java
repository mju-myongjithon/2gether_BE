package com.twogether.backend.gathering.service;

import com.twogether.backend.department.repository.DepartmentRepository;
import com.twogether.backend.gathering.domain.Gathering;
import com.twogether.backend.gathering.domain.GatheringStatus;
import com.twogether.backend.gathering.repository.GatheringImageRepository;
import com.twogether.backend.gathering.repository.GatheringMemberRepository;
import com.twogether.backend.gathering.repository.GatheringRepository;
import com.twogether.backend.gathering.repository.GatheringTagRepository;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.tag.repository.TagRepository;
import com.twogether.backend.user.domain.User;
import com.twogether.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GatheringCompletionTest {

    private GatheringRepository gatheringRepository;
    private UserRepository userRepository;
    private GatheringService gatheringService;

    @BeforeEach
    void setUp() {
        gatheringRepository = mock(GatheringRepository.class);
        userRepository = mock(UserRepository.class);
        gatheringService = new GatheringService(
                gatheringRepository,
                mock(GatheringMemberRepository.class),
                mock(GatheringTagRepository.class),
                mock(GatheringImageRepository.class),
                mock(TagRepository.class),
                userRepository,
                mock(DepartmentRepository.class)
        );
    }

    @Test
    void hostCompletesConfirmedGathering() {
        User user = mock(User.class);
        Gathering gathering = mock(Gathering.class);
        when(user.getId()).thenReturn(1L);
        when(gathering.isHost(1L)).thenReturn(true);
        when(gathering.getId()).thenReturn(10L);
        when(gathering.getStatus()).thenReturn(GatheringStatus.COMPLETED);
        when(userRepository.findByAuthUserId("auth-id")).thenReturn(Optional.of(user));
        when(gatheringRepository.findById(10L)).thenReturn(Optional.of(gathering));

        var response = gatheringService.complete("auth-id", 10L);

        verify(gathering).complete();
        assertThat(response.status()).isEqualTo(GatheringStatus.COMPLETED);
    }

    @Test
    void nonHostCannotCompleteGathering() {
        User user = mock(User.class);
        Gathering gathering = mock(Gathering.class);
        when(user.getId()).thenReturn(2L);
        when(gathering.isHost(2L)).thenReturn(false);
        when(userRepository.findByAuthUserId("auth-id")).thenReturn(Optional.of(user));
        when(gatheringRepository.findById(10L)).thenReturn(Optional.of(gathering));

        assertThatThrownBy(() -> gatheringService.complete("auth-id", 10L))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.GATHERING_COMPLETE_FORBIDDEN);
    }

    @Test
    void gatheringOtherThanConfirmedCannotBeCompleted() {
        Gathering gathering = mock(Gathering.class);
        when(gathering.getStatus()).thenReturn(GatheringStatus.RECRUITING);
        org.mockito.Mockito.doThrow(
                new BusinessException(ErrorCode.INVALID_GATHERING_STATUS_FOR_COMPLETE)
        ).when(gathering).complete();
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(gathering.isHost(1L)).thenReturn(true);
        when(userRepository.findByAuthUserId("auth-id")).thenReturn(Optional.of(user));
        when(gatheringRepository.findById(10L)).thenReturn(Optional.of(gathering));

        assertThatThrownBy(() -> gatheringService.complete("auth-id", 10L))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_GATHERING_STATUS_FOR_COMPLETE);
    }
}
