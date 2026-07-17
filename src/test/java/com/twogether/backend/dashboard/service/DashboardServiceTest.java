package com.twogether.backend.dashboard.service;

import com.twogether.backend.gathering.domain.GatheringStatus;
import com.twogether.backend.gathering.repository.GatheringMemberRepository;
import com.twogether.backend.gathering.repository.GatheringRepository;
import com.twogether.backend.verification.repository.VerificationRepository;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DashboardServiceTest {
    private final VerificationRepository verificationRepository = mock(VerificationRepository.class);
    private final GatheringRepository gatheringRepository = mock(GatheringRepository.class);
    private final GatheringMemberRepository memberRepository = mock(GatheringMemberRepository.class);
    private final DashboardService service = new DashboardService(verificationRepository, gatheringRepository,
            memberRepository, Clock.fixed(Instant.parse("2026-07-17T00:00:00Z"), ZoneOffset.UTC));

    @Test
    void statisticsUseSeoulMonthBoundaryAndDistinctParticipants() {
        when(verificationRepository.countApprovedBetween(any(), any())).thenReturn(24L);
        when(gatheringRepository.countByConfirmedAtGreaterThanEqualAndConfirmedAtLessThanAndStatusIn(any(), any(), any()))
                .thenReturn(18L);
        when(verificationRepository.findDistinctApprovedGatheringIdsBetween(any(), any())).thenReturn(List.of(1L, 2L));
        when(memberRepository.countDistinctUsersByGatheringIds(List.of(1L, 2L))).thenReturn(63L);

        var response = service.getStatistics();

        assertThat(response.month()).isEqualTo("2026-07");
        assertThat(response.monthlyInteractionCount()).isEqualTo(24);
        assertThat(response.matchedGroupCount()).isEqualTo(18);
        assertThat(response.participantCount()).isEqualTo(63);
        OffsetDateTime start = OffsetDateTime.parse("2026-07-01T00:00:00+09:00");
        OffsetDateTime end = OffsetDateTime.parse("2026-08-01T00:00:00+09:00");
        verify(verificationRepository).countApprovedBetween(start, end);
        verify(gatheringRepository).countByConfirmedAtGreaterThanEqualAndConfirmedAtLessThanAndStatusIn(
                start, end, List.of(GatheringStatus.CONFIRMED, GatheringStatus.COMPLETED));
    }

    @Test
    void noDataReturnsZerosWithoutEmptyInQuery() {
        when(verificationRepository.findDistinctApprovedGatheringIdsBetween(any(), any())).thenReturn(List.of());

        var response = service.getStatistics();

        assertThat(response.monthlyInteractionCount()).isZero();
        assertThat(response.matchedGroupCount()).isZero();
        assertThat(response.participantCount()).isZero();
        verify(memberRepository, never()).countDistinctUsersByGatheringIds(anyList());
    }
}
