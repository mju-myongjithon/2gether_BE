package com.twogether.backend.gatheringinvitation.service;

import com.twogether.backend.department.domain.College;
import com.twogether.backend.department.repository.DepartmentRepository;
import com.twogether.backend.gathering.domain.Gathering;
import com.twogether.backend.gathering.repository.GatheringRepository;
import com.twogether.backend.gatheringinvitation.domain.GatheringInvitation;
import com.twogether.backend.gatheringinvitation.domain.InvitationStatus;
import com.twogether.backend.gatheringinvitation.dto.request.GatheringInvitationCreateRequest;
import com.twogether.backend.gatheringinvitation.dto.response.GatheringInvitationResponse;
import com.twogether.backend.gatheringinvitation.repository.GatheringInvitationRepository;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.gathering.repository.GatheringMemberRepository;
import com.twogether.backend.user.domain.User;
import com.twogether.backend.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class GatheringInvitationService {

    private final GatheringInvitationRepository invitationRepository;
    private final GatheringRepository gatheringRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final GatheringMemberRepository memberRepository;

    public GatheringInvitationService(
            GatheringInvitationRepository invitationRepository,
            GatheringRepository gatheringRepository,
            UserRepository userRepository,
            DepartmentRepository departmentRepository,
            GatheringMemberRepository memberRepository
    ) {
        this.invitationRepository = invitationRepository;
        this.gatheringRepository = gatheringRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.memberRepository = memberRepository;
    }

    public GatheringInvitationResponse invite(
            String inviterId,
            Long gatheringId,
            GatheringInvitationCreateRequest request
    ) {
        User inviter = userRepository.findByAuthUserId(inviterId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GATHERING_NOT_FOUND));

        if (!gathering.getHost().getId().equals(inviter.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        User invitee = userRepository.findById(request.inviteeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (memberRepository.existsByGatheringIdAndUserId(gatheringId, invitee.getId())) {
            throw new BusinessException(ErrorCode.ALREADY_MEMBER);
        }

        if (invitationRepository.existsByGatheringIdAndInviteeIdAndStatusIn(
                gatheringId,
                invitee.getId(),
                List.of(InvitationStatus.PENDING, InvitationStatus.ACCEPTED)
        )) {
            throw new BusinessException(ErrorCode.DUPLICATE_APPLICATION);
        }

        GatheringInvitation invitation = GatheringInvitation.create(
                gathering,
                inviter,
                invitee,
                request.message()
        );

        invitationRepository.save(invitation);

        return toResponse(invitation);
    }

    @Transactional(readOnly = true)
    public Page<GatheringInvitationResponse> getSentInvitations(
            String inviterId,
            Long gatheringId,
            String status,
            Pageable pageable
    ) {
        User inviter = userRepository.findByAuthUserId(inviterId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GATHERING_NOT_FOUND));

        if (!gathering.getHost().getId().equals(inviter.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        Page<GatheringInvitation> invitations;
        if (status != null && !status.isEmpty()) {
            InvitationStatus invitationStatus = InvitationStatus.valueOf(status.toUpperCase());
            invitations = invitationRepository.findByGatheringIdAndInviterIdAndStatus(
                    gatheringId,
                    inviter.getId(),
                    invitationStatus,
                    pageable
            );
        } else {
            invitations = invitationRepository.findByGatheringIdAndInviterId(
                    gatheringId,
                    inviter.getId(),
                    pageable
            );
        }

        return invitations.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<GatheringInvitationResponse> getReceivedInvitations(
            String inviteeId,
            String status,
            Pageable pageable
    ) {
        User invitee = userRepository.findByAuthUserId(inviteeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Page<GatheringInvitation> invitations;
        if (status != null && !status.isEmpty()) {
            InvitationStatus invitationStatus = InvitationStatus.valueOf(status.toUpperCase());
            invitations = invitationRepository.findByInviteeIdAndStatus(
                    invitee.getId(),
                    invitationStatus,
                    pageable
            );
        } else {
            invitations = invitationRepository.findByInviteeId(
                    invitee.getId(),
                    pageable
            );
        }

        return invitations.map(this::toResponse);
    }

    public GatheringInvitationResponse accept(
            String userId,
            Long invitationId
    ) {
        User user = userRepository.findByAuthUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        GatheringInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND));

        if (!invitation.getInvitee().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (!invitation.isPending()) {
            throw new BusinessException(ErrorCode.APPLICATION_ALREADY_PROCESSED);
        }

        Gathering gathering = invitation.getGathering();
        if (gathering.isFull()) {
            throw new BusinessException(ErrorCode.CAPACITY_EXCEEDED);
        }

        invitation.accept();
        invitationRepository.save(invitation);

        return toResponse(invitation);
    }

    public GatheringInvitationResponse reject(
            String userId,
            Long invitationId,
            String reason
    ) {
        User user = userRepository.findByAuthUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        GatheringInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND));

        if (!invitation.getInvitee().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (!invitation.isPending()) {
            throw new BusinessException(ErrorCode.APPLICATION_ALREADY_PROCESSED);
        }

        invitation.reject(reason);
        invitationRepository.save(invitation);

        return toResponse(invitation);
    }

    public void cancel(
            String userId,
            Long invitationId
    ) {
        User user = userRepository.findByAuthUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        GatheringInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND));

        if (!invitation.getInviter().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (!invitation.isPending()) {
            throw new BusinessException(ErrorCode.APPLICATION_ALREADY_PROCESSED);
        }

        invitation.cancel();
        invitationRepository.save(invitation);
    }

    private GatheringInvitationResponse toResponse(GatheringInvitation invitation) {
        String inviterDepartmentName = getDepartmentName(invitation.getInviter().getDepartmentId());
        String inviterCampus = getCampus(invitation.getInviter().getDepartmentId());
        String inviteeDepartmentName = getDepartmentName(invitation.getInvitee().getDepartmentId());
        String inviteeCampus = getCampus(invitation.getInvitee().getDepartmentId());

        return GatheringInvitationResponse.of(
                invitation,
                inviterDepartmentName,
                inviterCampus,
                inviteeDepartmentName,
                inviteeCampus
        );
    }

    private String getDepartmentName(Long departmentId) {
        if (departmentId == null) return null;
        return departmentRepository.findById(departmentId)
                .map(dept -> dept.getName())
                .orElse(null);
    }

    private String getCampus(Long departmentId) {
        if (departmentId == null) return null;
        return departmentRepository.findById(departmentId)
                .map(dept -> dept.getCollege().getCampus().name())
                .orElse(null);
    }
}
