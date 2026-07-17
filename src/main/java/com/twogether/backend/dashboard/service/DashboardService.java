package com.twogether.backend.dashboard.service;

import com.twogether.backend.dashboard.dto.response.DashboardStatisticsResponse;
import com.twogether.backend.gathering.domain.GatheringStatus;
import com.twogether.backend.gathering.repository.GatheringMemberRepository;
import com.twogether.backend.gathering.repository.GatheringRepository;
import com.twogether.backend.verification.repository.VerificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DashboardService {
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private final VerificationRepository verificationRepository;
    private final GatheringRepository gatheringRepository;
    private final GatheringMemberRepository memberRepository;
    private final Clock clock;

    public DashboardService(VerificationRepository verificationRepository, GatheringRepository gatheringRepository,
                            GatheringMemberRepository memberRepository, Clock clock) {
        this.verificationRepository = verificationRepository;
        this.gatheringRepository = gatheringRepository;
        this.memberRepository = memberRepository;
        this.clock = clock;
    }

    public DashboardStatisticsResponse getStatistics() {
        YearMonth month = YearMonth.now(clock.withZone(SEOUL));
        OffsetDateTime start = month.atDay(1).atStartOfDay(SEOUL).toOffsetDateTime();
        OffsetDateTime end = month.plusMonths(1).atDay(1).atStartOfDay(SEOUL).toOffsetDateTime();
        long interactions = verificationRepository.countApprovedBetween(start, end);
        long groups = gatheringRepository.countByConfirmedAtGreaterThanEqualAndConfirmedAtLessThanAndStatusIn(
                start, end, List.of(GatheringStatus.CONFIRMED, GatheringStatus.COMPLETED));
        List<Long> gatheringIds = verificationRepository.findDistinctApprovedGatheringIdsBetween(start, end);
        long participants = gatheringIds.isEmpty() ? 0 : memberRepository.countDistinctUsersByGatheringIds(gatheringIds);
        return new DashboardStatisticsResponse(month.toString(), interactions, groups, participants);
    }
}
