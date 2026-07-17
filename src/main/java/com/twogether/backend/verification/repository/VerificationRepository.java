package com.twogether.backend.verification.repository;

import com.twogether.backend.verification.domain.Verification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface VerificationRepository extends JpaRepository<Verification, Long> {
    boolean existsByGatheringId(Long gatheringId);

    @Query("select v.verifiedAt from Verification v where v.aiStatus = com.twogether.backend.verification.domain.AiStatus.APPROVED and v.verifiedAt >= :start and v.verifiedAt < :end")
    List<OffsetDateTime> findApprovedVerifiedAtBetween(@Param("start") OffsetDateTime start,
                                                       @Param("end") OffsetDateTime end);

    @Query("select v from Verification v join fetch v.gathering where v.aiStatus = com.twogether.backend.verification.domain.AiStatus.APPROVED and v.verifiedAt >= :start and v.verifiedAt < :end order by v.verifiedAt asc, v.id asc")
    List<Verification> findApprovedActivitiesBetween(@Param("start") OffsetDateTime start,
                                                      @Param("end") OffsetDateTime end);

    @Query("select count(v) from Verification v where v.aiStatus = com.twogether.backend.verification.domain.AiStatus.APPROVED and v.verifiedAt >= :start and v.verifiedAt < :end")
    long countApprovedBetween(@Param("start") OffsetDateTime start, @Param("end") OffsetDateTime end);

    @Query("select distinct v.gathering.id from Verification v where v.aiStatus = com.twogether.backend.verification.domain.AiStatus.APPROVED and v.verifiedAt >= :start and v.verifiedAt < :end")
    List<Long> findDistinctApprovedGatheringIdsBetween(@Param("start") OffsetDateTime start,
                                                       @Param("end") OffsetDateTime end);
}
