package com.twogether.backend.gatheringmember.service;

import com.twogether.backend.department.domain.Department;
import com.twogether.backend.department.repository.DepartmentRepository;
import com.twogether.backend.gathering.domain.GatheringMember;
import com.twogether.backend.gatheringmember.domain.GatheringMemberRole;
import com.twogether.backend.gathering.repository.GatheringMemberRepository;
import com.twogether.backend.gathering.repository.GatheringRepository;
import com.twogether.backend.gatheringapplication.domain.ApplicationStatus;
import com.twogether.backend.gatheringapplication.domain.GatheringApplication;
import com.twogether.backend.gatheringapplication.repository.GatheringApplicationRepository;
import com.twogether.backend.gatheringmember.dto.response.GatheringMemberListResponse;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.global.response.PageResponse;
import com.twogether.backend.user.domain.User;
import com.twogether.backend.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class GatheringMemberService {

    private final GatheringMemberRepository gatheringMemberRepository;
    private final GatheringRepository gatheringRepository;
        private final GatheringApplicationRepository gatheringApplicationRepository;
        private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    public GatheringMemberService(
            GatheringMemberRepository gatheringMemberRepository,
            GatheringRepository gatheringRepository,
                        GatheringApplicationRepository gatheringApplicationRepository,
                        UserRepository userRepository,
            DepartmentRepository departmentRepository
    ) {
        this.gatheringMemberRepository = gatheringMemberRepository;
        this.gatheringRepository = gatheringRepository;
                this.gatheringApplicationRepository = gatheringApplicationRepository;
                this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
    }

    /**
     * 모임 멤버 목록 조회(페이징).
     *
     * 사용자(user)를 함께 로딩하고 학과명은 배치로 매핑한다.
     * 정렬은 HOST 우선(role STRING 오름차순: HOST < MEMBER) + 참여순(joined_at).
     * 캠퍼스 비율 기능 제외로 campus 는 null.
     */
    public PageResponse<GatheringMemberListResponse> getMembers(
            Long gatheringId,
            int page,
            int size
    ) {
        if (!gatheringRepository.existsById(gatheringId)) {
            throw new BusinessException(ErrorCode.GATHERING_NOT_FOUND);
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Order.asc("role"), Sort.Order.asc("joinedAt"))
        );

        Page<GatheringMember> memberPage = gatheringMemberRepository.findByGatheringId(gatheringId, pageable);
        List<GatheringMember> members = memberPage.getContent();

        Map<Long, String> departmentNameById = resolveDepartmentNames(members);

        List<GatheringMemberListResponse> content = members.stream()
                .map(member -> toResponse(member, departmentNameById))
                .toList();

        return PageResponse.of(
                content,
                page,
                size,
                memberPage.getTotalElements()
        );
    }

    /**
     * 방장이 특정 멤버를 추방한다.
     *
     * 멤버 레코드와 연결된 ACCEPTED 신청 레코드를 함께 삭제해,
     * 추방된 사용자가 이후 다시 신청할 수 있게 한다.
     */
    @Transactional
    public void expelMember(
            String authUserId,
            Long gatheringId,
            Long userId
    ) {
        User me = userRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        removeMemberFromGathering(me, gatheringId, userId, true);
    }

    /**
     * 모임에서 나간다.
     *
     * 현재 사용자 본인만 나갈 수 있으며, 방장은 나갈 수 없다.
     */
    @Transactional
    public void leaveMember(
            String authUserId,
            Long gatheringId
    ) {
        User me = userRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

                removeMemberFromGathering(me, gatheringId, me.getId(), false);
    }

    private void removeMemberFromGathering(
                        User me,
            Long gatheringId,
                        Long userId,
                        boolean requireHostPrivilege
    ) {
        com.twogether.backend.gathering.domain.Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GATHERING_NOT_FOUND));

                if (requireHostPrivilege) {
                        if (!gathering.isHost(me.getId()) || gathering.isHost(userId)) {
                                throw new BusinessException(ErrorCode.FORBIDDEN);
                        }
                } else {
                        if (gathering.isHost(me.getId()) || !me.getId().equals(userId)) {
                                throw new BusinessException(ErrorCode.FORBIDDEN);
                        }
                }

                GatheringMember target = gatheringMemberRepository.findByGatheringIdAndUserId(gatheringId, userId)
                                .orElseThrow(() -> new BusinessException(ErrorCode.GATHERING_MEMBER_NOT_FOUND));

                if (target.getRole() == GatheringMemberRole.HOST) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        GatheringApplication application = gatheringApplicationRepository
                .findByGatheringIdAndUserIdAndStatus(gatheringId, userId, ApplicationStatus.ACCEPTED)
                .orElse(null);

        if (application != null) {
            gatheringApplicationRepository.deleteByGatheringIdAndUserIdAndStatus(
                    gatheringId,
                    userId,
                    ApplicationStatus.ACCEPTED
            );
        }

        gatheringMemberRepository.deleteByGatheringIdAndUserId(gatheringId, userId);
        gathering.decreaseMember();
    }

    private GatheringMemberListResponse toResponse(
            GatheringMember member,
            Map<Long, String> departmentNameById
    ) {
        User user = member.getUser();

        return new GatheringMemberListResponse(
                user.getId(),
                user.getNickname(),
                member.getRole(),
                departmentNameById.get(user.getDepartmentId()),
                // 캠퍼스 비율 기능 제외 → campus null
                null,
                member.getJoinedAt()
        );
    }

    private Map<Long, String> resolveDepartmentNames(
            List<GatheringMember> members
    ) {
        List<Long> departmentIds = members.stream()
                .map(member -> member.getUser().getDepartmentId())
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (departmentIds.isEmpty()) {
            return Map.of();
        }

        return departmentRepository.findAllById(departmentIds).stream()
                .collect(Collectors.toMap(Department::getId, Department::getName));
    }
}
