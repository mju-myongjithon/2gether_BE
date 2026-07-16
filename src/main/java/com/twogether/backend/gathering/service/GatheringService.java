package com.twogether.backend.gathering.service;

import com.twogether.backend.gathering.domain.Gathering;
import com.twogether.backend.gathering.domain.GatheringImage;
import com.twogether.backend.gathering.domain.GatheringMember;
import com.twogether.backend.gathering.domain.GatheringTag;
import com.twogether.backend.gathering.dto.request.GatheringCreateRequest;
import com.twogether.backend.gathering.dto.response.GatheringCreateResponse;
import com.twogether.backend.gathering.repository.GatheringImageRepository;
import com.twogether.backend.gathering.repository.GatheringMemberRepository;
import com.twogether.backend.gathering.repository.GatheringRepository;
import com.twogether.backend.gathering.repository.GatheringTagRepository;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.tag.repository.TagRepository;
import com.twogether.backend.user.domain.User;
import com.twogether.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class GatheringService {

    private final GatheringRepository gatheringRepository;
    private final GatheringMemberRepository gatheringMemberRepository;
    private final GatheringTagRepository gatheringTagRepository;
    private final GatheringImageRepository gatheringImageRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;

    public GatheringService(
            GatheringRepository gatheringRepository,
            GatheringMemberRepository gatheringMemberRepository,
            GatheringTagRepository gatheringTagRepository,
            GatheringImageRepository gatheringImageRepository,
            TagRepository tagRepository,
            UserRepository userRepository
    ) {
        this.gatheringRepository = gatheringRepository;
        this.gatheringMemberRepository = gatheringMemberRepository;
        this.gatheringTagRepository = gatheringTagRepository;
        this.gatheringImageRepository = gatheringImageRepository;
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public GatheringCreateResponse create(
            String authUserId,
            GatheringCreateRequest request
    ) {
        User host = userRepository
                .findByAuthUserId(authUserId)
                .orElseThrow(
                        () -> new BusinessException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );

        Gathering gathering = new Gathering(
                host,
                request.title(),
                request.content(),
                request.category(),
                request.location(),
                (short) request.maxMembers(),
                request.fusionEnabled(),
                request.recruitStartAt(),
                request.recruitEndAt(),
                request.meetAt()
        );
        gatheringRepository.save(gathering);

        // 방장을 HOST 멤버로 등록 (current_members는 엔티티 생성 시 1로 시작)
        gatheringMemberRepository.save(
                GatheringMember.host(gathering, host)
        );

        saveTags(gathering, request.tagIds());
        saveImages(gathering, request.imageUrls());

        return GatheringCreateResponse.from(gathering);
    }

    private void saveTags(
            Gathering gathering,
            List<Long> tagIds
    ) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }

        List<Long> distinctTagIds = tagIds.stream()
                .distinct()
                .toList();

        long foundCount = tagRepository
                .findAllById(distinctTagIds)
                .size();

        if (foundCount != distinctTagIds.size()) {
            throw new BusinessException(ErrorCode.INVALID_TAG);
        }

        List<GatheringTag> gatheringTags = distinctTagIds.stream()
                .map(tagId -> new GatheringTag(gathering, tagId))
                .toList();

        gatheringTagRepository.saveAll(gatheringTags);
    }

    private void saveImages(
            Gathering gathering,
            List<String> imageUrls
    ) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }

        List<GatheringImage> images = new ArrayList<>();
        short sortOrder = 0;
        for (String imageUrl : imageUrls) {
            images.add(
                    new GatheringImage(gathering, imageUrl, sortOrder)
            );
            sortOrder++;
        }

        gatheringImageRepository.saveAll(images);
    }
}
