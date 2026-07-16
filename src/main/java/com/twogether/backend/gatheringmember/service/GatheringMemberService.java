package com.twogether.backend.gatheringmember.service;

import com.twogether.backend.department.domain.Department;
import com.twogether.backend.department.repository.DepartmentRepository;
import com.twogether.backend.gathering.domain.GatheringMember;
import com.twogether.backend.gathering.repository.GatheringMemberRepository;
import com.twogether.backend.gathering.repository.GatheringRepository;
import com.twogether.backend.gatheringmember.dto.response.GatheringMemberListResponse;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.global.response.PageResponse;
import com.twogether.backend.user.domain.User;
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
    private final DepartmentRepository departmentRepository;

    public GatheringMemberService(
            GatheringMemberRepository gatheringMemberRepository,
            GatheringRepository gatheringRepository,
            DepartmentRepository departmentRepository
    ) {
        this.gatheringMemberRepository = gatheringMemberRepository;
        this.gatheringRepository = gatheringRepository;
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
