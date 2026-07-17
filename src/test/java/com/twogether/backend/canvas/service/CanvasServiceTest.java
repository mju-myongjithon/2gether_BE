package com.twogether.backend.canvas.service;

import com.twogether.backend.gathering.domain.Gathering;
import com.twogether.backend.user.domain.User;
import com.twogether.backend.verification.domain.Verification;
import com.twogether.backend.verification.repository.VerificationRepository;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CanvasServiceTest {
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private final VerificationRepository repository = mock(VerificationRepository.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-07-17T00:00:00Z"), ZoneOffset.UTC);
    private final CanvasService service = new CanvasService(repository, clock);

    @Test
    void contributionsContainExactly365OrderedDaysAndLevels() {
        OffsetDateTime day = OffsetDateTime.parse("2026-07-17T01:00:00+09:00");
        when(repository.findApprovedVerifiedAtBetween(any(), any())).thenReturn(List.of(
                day, day.plusHours(1), day.plusHours(2), day.plusHours(3), day.plusHours(4), day.plusHours(5)));

        var response = service.getContributions();

        assertThat(response.startDate()).isEqualTo(response.endDate().minusDays(364));
        assertThat(response.days()).hasSize(365).isSortedAccordingTo((a, b) -> a.date().compareTo(b.date()));
        assertThat(response.days().get(0).count()).isZero();
        assertThat(response.days().get(0).level()).isZero();
        assertThat(response.days().get(364).count()).isEqualTo(6);
        assertThat(response.days().get(364).level()).isEqualTo(4);
        assertThat(response.totalApprovedCount()).isEqualTo(6);
        verify(repository).findApprovedVerifiedAtBetween(
                eq(response.startDate().atStartOfDay(SEOUL).toOffsetDateTime()),
                eq(response.endDate().plusDays(1).atStartOfDay(SEOUL).toOffsetDateTime()));
    }

    @Test
    void levelPolicyMatchesCounts() {
        assertThat(CanvasService.level(0)).isZero();
        assertThat(CanvasService.level(1)).isEqualTo(1);
        assertThat(CanvasService.level(2)).isEqualTo(2);
        assertThat(CanvasService.level(3)).isEqualTo(2);
        assertThat(CanvasService.level(4)).isEqualTo(3);
        assertThat(CanvasService.level(5)).isEqualTo(3);
        assertThat(CanvasService.level(6)).isEqualTo(4);
    }

    @Test
    void dailyActivitiesUseSeoulDayBoundaryAndPreserveOrder() {
        LocalDate date = LocalDate.of(2026, 7, 17);
        Verification first = verification(1L, OffsetDateTime.parse("2026-07-17T09:00:00+09:00"));
        Verification second = verification(2L, OffsetDateTime.parse("2026-07-17T10:00:00+09:00"));
        when(repository.findApprovedActivitiesBetween(any(), any())).thenReturn(List.of(first, second));

        var response = service.getActivities(date);

        assertThat(response.count()).isEqualTo(2);
        assertThat(response.activities()).extracting(a -> a.verificationId()).containsExactly(1L, 2L);
        assertThat(response.activities().get(0).gatheringTitle()).isEqualTo("테스트 모임");
        verify(repository).findApprovedActivitiesBetween(
                eq(date.atStartOfDay(SEOUL).toOffsetDateTime()),
                eq(date.plusDays(1).atStartOfDay(SEOUL).toOffsetDateTime()));
    }

    @Test
    void emptyDateReturnsEmptyList() {
        when(repository.findApprovedActivitiesBetween(any(), any())).thenReturn(List.of());
        assertThat(service.getActivities(LocalDate.of(2030, 1, 1)).activities()).isEmpty();
    }

    private Verification verification(long id, OffsetDateTime verifiedAt) {
        Verification value = mock(Verification.class);
        Gathering gathering = mock(Gathering.class);
        User uploader = mock(User.class);
        when(value.getId()).thenReturn(id);
        when(value.getGathering()).thenReturn(gathering);
        when(value.getUploader()).thenReturn(uploader);
        when(gathering.getId()).thenReturn(10L + id);
        when(gathering.getTitle()).thenReturn("테스트 모임");
        when(uploader.getId()).thenReturn(20L + id);
        when(value.getReviewText()).thenReturn("활동 후기");
        when(value.getPhotoUrl()).thenReturn("photo");
        when(value.getVerifiedAt()).thenReturn(verifiedAt);
        return value;
    }
}
