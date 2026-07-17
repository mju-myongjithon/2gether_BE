package com.twogether.backend.gatheringnotice.service;

import com.twogether.backend.gathering.domain.Gathering;
import com.twogether.backend.gathering.repository.GatheringRepository;
import com.twogether.backend.gathering.repository.GatheringMemberRepository;
import com.twogether.backend.gatheringnotice.domain.GatheringNotice;
import com.twogether.backend.gatheringnotice.dto.request.NoticeCreateRequest;
import com.twogether.backend.gatheringnotice.dto.response.NoticeResponse;
import com.twogether.backend.gatheringnotice.repository.GatheringNoticeRepository;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.global.response.PageResponse;
import com.twogether.backend.user.domain.User;
import com.twogether.backend.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GatheringNoticeService {

    private final GatheringNoticeRepository gatheringNoticeRepository;
    private final GatheringRepository gatheringRepository;
    private final GatheringMemberRepository gatheringMemberRepository;
    private final UserRepository userRepository;

    public GatheringNoticeService(
            GatheringNoticeRepository gatheringNoticeRepository,
            GatheringRepository gatheringRepository,
            GatheringMemberRepository gatheringMemberRepository,
            UserRepository userRepository
    ) {
        this.gatheringNoticeRepository = gatheringNoticeRepository;
        this.gatheringRepository = gatheringRepository;
        this.gatheringMemberRepository = gatheringMemberRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public NoticeResponse create(
            String authUserId,
            Long gatheringId,
            NoticeCreateRequest request
    ) {
        User me = getUserByAuthId(authUserId);
        Gathering gathering = getGatheringById(gatheringId);

        if (!gathering.isHost(me.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        GatheringNotice notice = new GatheringNotice(
                gathering,
                request.title(),
                request.content(),
                request.isPinned()
        );

        GatheringNotice saved = gatheringNoticeRepository.save(notice);
        return NoticeResponse.from(saved);
    }

    @Transactional
    public NoticeResponse update(
            String authUserId,
            Long gatheringId,
            Long noticeId,
            NoticeCreateRequest request
    ) {
        User me = getUserByAuthId(authUserId);
        GatheringNotice notice = getNoticeById(noticeId);

        if (!notice.getGathering().getId().equals(gatheringId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        if (!notice.getGathering().isHost(me.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        notice.update(request.title(), request.content(), request.isPinned());
        return NoticeResponse.from(notice);
    }

    @Transactional
    public void delete(
            String authUserId,
            Long gatheringId,
            Long noticeId
    ) {
        User me = getUserByAuthId(authUserId);
        GatheringNotice notice = getNoticeById(noticeId);

        if (!notice.getGathering().getId().equals(gatheringId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        if (!notice.getGathering().isHost(me.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        gatheringNoticeRepository.delete(notice);
    }

    public PageResponse<NoticeResponse> getNotices(
            String authUserId,
            Long gatheringId,
            int page,
            int size
    ) {
        User me = getUserByAuthId(authUserId);
        Gathering gathering = getGatheringById(gatheringId);

        boolean isHost = gathering.isHost(me.getId());
        boolean isMember = gatheringMemberRepository.existsByGatheringIdAndUserId(gatheringId, me.getId());

        if (!isHost && !isMember) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<NoticeResponse> pageResult = gatheringNoticeRepository
                .findAllByGatheringId(gatheringId, pageable)
                .map(NoticeResponse::from);

        return PageResponse.of(
                pageResult.getContent(),
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements()
        );
    }

    private User getUserByAuthId(String authUserId) {
        return userRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private Gathering getGatheringById(Long gatheringId) {
        return gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GATHERING_NOT_FOUND));
    }

    private GatheringNotice getNoticeById(Long noticeId) {
        return gatheringNoticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));
    }
}
