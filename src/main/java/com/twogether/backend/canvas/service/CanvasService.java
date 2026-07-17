package com.twogether.backend.canvas.service;

import com.twogether.backend.canvas.dto.response.CanvasActivityResponse;
import com.twogether.backend.canvas.dto.response.CanvasContributionDayResponse;
import com.twogether.backend.canvas.dto.response.CanvasContributionResponse;
import com.twogether.backend.canvas.dto.response.CanvasDailyActivitiesResponse;
import com.twogether.backend.verification.repository.VerificationRepository;
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
public class CanvasService {
    static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private final VerificationRepository verificationRepository;
    private final Clock clock;

    public CanvasService(VerificationRepository verificationRepository, Clock clock) {
        this.verificationRepository = verificationRepository;
        this.clock = clock;
    }

    public CanvasContributionResponse getContributions() {
        LocalDate endDate = LocalDate.now(clock.withZone(SEOUL));
        LocalDate startDate = endDate.minusDays(364);
        OffsetDateTime start = startDate.atStartOfDay(SEOUL).toOffsetDateTime();
        OffsetDateTime end = endDate.plusDays(1).atStartOfDay(SEOUL).toOffsetDateTime();
        Map<LocalDate, Long> counts = verificationRepository.findApprovedVerifiedAtBetween(start, end)
                .stream().collect(Collectors.groupingBy(t -> t.atZoneSameInstant(SEOUL).toLocalDate(), Collectors.counting()));
        List<CanvasContributionDayResponse> days = LongStream.range(0, 365)
                .mapToObj(i -> startDate.plusDays(i))
                .map(date -> new CanvasContributionDayResponse(date, counts.getOrDefault(date, 0L),
                        level(counts.getOrDefault(date, 0L))))
                .toList();
        long total = days.stream().mapToLong(CanvasContributionDayResponse::count).sum();
        return new CanvasContributionResponse(startDate, endDate, total, days);
    }

    public CanvasDailyActivitiesResponse getActivities(LocalDate date) {
        OffsetDateTime start = date.atStartOfDay(SEOUL).toOffsetDateTime();
        OffsetDateTime end = date.plusDays(1).atStartOfDay(SEOUL).toOffsetDateTime();
        List<CanvasActivityResponse> activities = verificationRepository.findApprovedActivitiesBetween(start, end)
                .stream().map(CanvasActivityResponse::from).toList();
        return new CanvasDailyActivitiesResponse(date, activities.size(), activities);
    }

    static int level(long count) {
        if (count == 0) return 0;
        if (count == 1) return 1;
        if (count <= 3) return 2;
        if (count <= 5) return 3;
        return 4;
    }
}
