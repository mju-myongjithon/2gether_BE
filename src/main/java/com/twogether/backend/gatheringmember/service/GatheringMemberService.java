package com.twogether.backend.gatheringmember.service;

import com.twogether.backend.gathering.domain.Gathering;
import com.twogether.backend.gathering.repository.GatheringRepository;
import com.twogether.backend.gatheringmember.domain.GatheringMember;
import com.twogether.backend.gatheringmember.dto.response.GatheringMemberListResponse;
import com.twogether.backend.gatheringmember.repository.GatheringMemberRepository;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.global.response.PageResponse;
import com.twogether.backend.user.domain.User;
import com.twogether.backend.user.repository.UserRepository;
import com.twogether.backend.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class GatheringMemberService {

    private static final ZoneOffset KST = ZoneOffset.of("+09:00");

    private final GatheringMemberRepository gatheringMemberRepository;
    private final GatheringRepository gatheringRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public GatheringMemberService(
            GatheringMemberRepository gatheringMemberRepository,
            GatheringRepository gatheringRepository,
            UserRepository userRepository,
            UserService userService
    ) {
        this.gatheringMemberRepository = gatheringMemberRepository;
        this.gatheringRepository = gatheringRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    /**
     * 모임에 참여 중인(탈퇴하지 않은) 멤버 목록을 조회합니다. 로그인 없이 조회할 수 있습니다.
     */
    public PageResponse<GatheringMemberListResponse> getMembers(
            Long gatheringId,
            int page,
            int size
    ) {
        findGathering(gatheringId);

        Page<GatheringMember> members = gatheringMemberRepository
                .findByGatheringIdAndLeftAtIsNullOrderByJoinedAtAsc(gatheringId, PageRequest.of(page, size));

        List<GatheringMemberListResponse> content = members.getContent()
                .stream()
                .map(this::toMemberListResponse)
                .toList();

        return PageResponse.of(content, page, size, members.getTotalElements());
    }

    /**
     * 일반 멤버가 모임에서 나갑니다. 방장은 바로 나갈 수 없습니다.
     */
    @Transactional
    public void leave(
            String authUserId,
            Long gatheringId
    ) {
        User user = userService.findOrCreateUser(authUserId);
        findGathering(gatheringId);

        GatheringMember member = gatheringMemberRepository
                .findByGatheringIdAndUserIdAndLeftAtIsNull(gatheringId, user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_GATHERING_MEMBER));

        if (member.isHost()) {
            throw new BusinessException(ErrorCode.HOST_CANNOT_LEAVE);
        }

        member.leave();
    }

    private Gathering findGathering(Long gatheringId) {
        return gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GATHERING_NOT_FOUND));
    }

    private GatheringMemberListResponse toMemberListResponse(GatheringMember member) {
        return userRepository.findById(member.getUserId())
                .map(user -> new GatheringMemberListResponse(
                        user.getId(),
                        user.getNickname(),
                        member.getRole(),

                        // TODO: Department 엔티티가 아직 없어 학과명/캠퍼스는 임시로 null
                        null,
                        null,
                        toOffsetDateTime(member.getJoinedAt())
                ))
                .orElse(new GatheringMemberListResponse(
                        member.getUserId(), null, member.getRole(), null, null,
                        toOffsetDateTime(member.getJoinedAt())
                ));
    }

    private static OffsetDateTime toOffsetDateTime(LocalDateTime localDateTime) {
        return localDateTime == null
                ? null
                : localDateTime.atOffset(KST);
    }
}
