package com.twogether.backend.gatheringinvitation.repository;

import com.twogether.backend.gatheringinvitation.domain.GatheringInvitation;
import com.twogether.backend.gatheringinvitation.domain.InvitationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GatheringInvitationRepository extends JpaRepository<GatheringInvitation, Long> {

    Optional<GatheringInvitation> findById(Long id);

    boolean existsByGatheringIdAndInviteeIdAndStatusIn(
            Long gatheringId,
            Long inviteeId,
            List<InvitationStatus> statuses
    );

    @Query("SELECT gi FROM GatheringInvitation gi WHERE gi.gathering.id = :gatheringId AND gi.inviter.id = :inviterId ORDER BY gi.invitedAt DESC")
    Page<GatheringInvitation> findByGatheringIdAndInviterId(
            @Param("gatheringId") Long gatheringId,
            @Param("inviterId") Long inviterId,
            Pageable pageable
    );

    @Query("SELECT gi FROM GatheringInvitation gi WHERE gi.gathering.id = :gatheringId AND gi.inviter.id = :inviterId AND gi.status = :status ORDER BY gi.invitedAt DESC")
    Page<GatheringInvitation> findByGatheringIdAndInviterIdAndStatus(
            @Param("gatheringId") Long gatheringId,
            @Param("inviterId") Long inviterId,
            @Param("status") InvitationStatus status,
            Pageable pageable
    );

    @Query("SELECT gi FROM GatheringInvitation gi WHERE gi.invitee.id = :inviteeId ORDER BY gi.invitedAt DESC")
    Page<GatheringInvitation> findByInviteeId(
            @Param("inviteeId") Long inviteeId,
            Pageable pageable
    );

    @Query("SELECT gi FROM GatheringInvitation gi WHERE gi.invitee.id = :inviteeId AND gi.status = :status ORDER BY gi.invitedAt DESC")
    Page<GatheringInvitation> findByInviteeIdAndStatus(
            @Param("inviteeId") Long inviteeId,
            @Param("status") InvitationStatus status,
            Pageable pageable
    );

    @Query("SELECT COUNT(gi) FROM GatheringInvitation gi WHERE gi.invitee.id = :inviteeId AND gi.status = :status")
    long countByInviteeIdAndStatus(
            @Param("inviteeId") Long inviteeId,
            @Param("status") InvitationStatus status
    );
}
