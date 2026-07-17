package com.twogether.backend.recommendation.service;

import com.twogether.backend.gathering.domain.Gathering;
import com.twogether.backend.gathering.domain.GatheringMember;
import com.twogether.backend.gathering.domain.GatheringTag;
import com.twogether.backend.gathering.repository.GatheringMemberRepository;
import com.twogether.backend.gathering.repository.GatheringRepository;
import com.twogether.backend.gathering.repository.GatheringTagRepository;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.recommendation.client.AiRecommendationClient;
import com.twogether.backend.recommendation.dto.request.AiCandidateInfo;
import com.twogether.backend.recommendation.dto.request.AiGatheringInfo;
import com.twogether.backend.recommendation.dto.request.AiGatheringRecommendationRequest;
import com.twogether.backend.recommendation.dto.request.AiTagInfo;
import com.twogether.backend.recommendation.dto.response.AiRecommendationResponse;
import com.twogether.backend.tag.domain.UserTag;
import com.twogether.backend.tag.repository.TagRepository;
import com.twogether.backend.tag.repository.UserTagRepository;
import com.twogether.backend.user.domain.User;
import com.twogether.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class GatheringRecommendationService {

    private static final int MAX_CANDIDATE_COUNT = 20;

    private final AiRecommendationClient aiRecommendationClient;
    private final GatheringRepository gatheringRepository;
    private final GatheringTagRepository gatheringTagRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final GatheringMemberRepository gatheringMemberRepository;
    private final UserTagRepository userTagRepository;

    public GatheringRecommendationService(
            AiRecommendationClient aiRecommendationClient,
            GatheringRepository gatheringRepository,
            GatheringTagRepository gatheringTagRepository,
            TagRepository tagRepository,
            UserRepository userRepository,
            GatheringMemberRepository gatheringMemberRepository,
            UserTagRepository userTagRepository
    ) {
        this.aiRecommendationClient = aiRecommendationClient;
        this.gatheringRepository = gatheringRepository;
        this.gatheringTagRepository = gatheringTagRepository;
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
        this.gatheringMemberRepository = gatheringMemberRepository;
        this.userTagRepository = userTagRepository;
    }

    public AiRecommendationResponse recommend(
            Long gatheringId,
            AiGatheringRecommendationRequest request
    ) {
        validateRequest(request);

        List<GatheringTag> gatheringTags = gatheringTagRepository
                .findAllByGathering_Id(gatheringId);

        AiGatheringInfo gatheringInfo =
                createGatheringInfo(gatheringId, gatheringTags);

        List<User> candidateUsers =
                findCandidateUsers(gatheringId);

        List<AiCandidateInfo> candidates =
                createCandidateInfos(candidateUsers, gatheringTags);

        if (candidates.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.RECOMMENDATION_CANDIDATE_EMPTY
            );
        }

        if (request.recommendationCount() > candidates.size()) {
            throw new BusinessException(
                    ErrorCode.INVALID_RECOMMENDATION_COUNT
            );
        }

        AiGatheringRecommendationRequest aiRequest =
                new AiGatheringRecommendationRequest(
                        gatheringInfo,
                        candidates,
                        request.recommendationCount()
                );

        return aiRecommendationClient.recommendMembers(
                aiRequest
        );
    }

    private void validateRequest(
            AiGatheringRecommendationRequest request
    ) {
        if (request == null
                || request.gathering() == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (request.recommendationCount() < 1) {
            throw new BusinessException(
                    ErrorCode.INVALID_RECOMMENDATION_COUNT
            );
        }
    }

    private AiGatheringInfo createGatheringInfo(
            Long gatheringId,
            List<GatheringTag> gatheringTags
    ) {
        Gathering gathering = gatheringRepository
                .findById(gatheringId)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.GATHERING_NOT_FOUND
                        )
                );

        List<Long> tagIds = gatheringTags
                .stream()
                .map(GatheringTag::getTagId)
                .toList();

        List<AiTagInfo> tags = tagRepository
                .findAllById(tagIds)
                .stream()
                .map(AiTagInfo::from)
                .toList();

        return AiGatheringInfo.from(
                gathering,
                tags
        );
    }

    private List<AiCandidateInfo> createCandidateInfos(
            List<User> candidateUsers,
            List<GatheringTag> gatheringTags
    ) {
        if (candidateUsers.isEmpty()) {
            return List.of();
        }

        List<Long> candidateUserIds = candidateUsers.stream()
                .map(User::getId)
                .toList();

        Map<Long, List<UserTag>> userTagsByUserId = userTagRepository
                .findAllByUserIdsWithTag(candidateUserIds)
                .stream()
                .collect(Collectors.groupingBy(
                        userTag -> userTag.getUser().getId()
                ));

        Set<Long> gatheringTagIds = gatheringTags.stream()
                .map(GatheringTag::getTagId)
                .collect(Collectors.toSet());

        return candidateUsers.stream()
                .map(user -> {
                    List<UserTag> userTags = userTagsByUserId.getOrDefault(
                            user.getId(),
                            Collections.emptyList()
                    );
                    int exactMatchCount = (int) userTags.stream()
                            .map(UserTag::getTag)
                            .map(tag -> tag.getId())
                            .filter(gatheringTagIds::contains)
                            .count();
                    List<AiTagInfo> tags = userTags.stream()
                            .map(UserTag::getTag)
                            .map(AiTagInfo::from)
                            .toList();

                    return AiCandidateInfo.from(
                            user,
                            exactMatchCount,
                            tags
                    );
                })
                .toList();
    }

    private List<User> findCandidateUsers(
            Long gatheringId
    ) {
        Set<Long> memberUserIds =
                gatheringMemberRepository
                        .findByGatheringIdWithUser(gatheringId)
                        .stream()
                        .map(GatheringMember::getUser)
                        .map(User::getId)
                        .collect(Collectors.toSet());

        return userRepository.findAll()
                .stream()
                .filter(User::isProfileCompleted)
                .filter(User::isEmailVerified)
                .filter(user ->
                        !memberUserIds.contains(user.getId())
                )
                .limit(MAX_CANDIDATE_COUNT)
                .toList();
    }
}
