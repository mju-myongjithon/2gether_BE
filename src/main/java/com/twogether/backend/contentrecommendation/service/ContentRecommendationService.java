package com.twogether.backend.contentrecommendation.service;

import com.twogether.backend.contentrecommendation.client.AiContentRecommendationClient;
import com.twogether.backend.contentrecommendation.client.ContentRecommendationContext;
import com.twogether.backend.contentrecommendation.client.RecommendedContent;
import com.twogether.backend.contentrecommendation.dto.ContentRecommendationResponse;
import com.twogether.backend.contentrecommendation.dto.RecommendedContentResponse;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.tag.repository.UserTagRepository;
import com.twogether.backend.user.domain.User;
import com.twogether.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

@Service
@Transactional(readOnly = true)
public class ContentRecommendationService {

    public static final int DEFAULT_LIMIT = 6;
    private final UserRepository userRepository;
    private final UserTagRepository userTagRepository;
    private final AiContentRecommendationClient recommendationClient;

    public ContentRecommendationService(UserRepository userRepository, UserTagRepository userTagRepository,
                                        AiContentRecommendationClient recommendationClient) {
        this.userRepository = userRepository;
        this.userTagRepository = userTagRepository;
        this.recommendationClient = recommendationClient;
    }

    public ContentRecommendationResponse recommend(String authUserId, int limit) {
        if (limit < 1 || limit > 10) throw new BusinessException(ErrorCode.INVALID_REQUEST);
        User user = userRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        List<String> interests = userTagRepository.findAllByUser_IdOrderByTag_IdAsc(user.getId()).stream()
                .map(userTag -> userTag.getTag().getName()).distinct().toList();
        List<RecommendedContent> result = recommendationClient.recommend(
                new ContentRecommendationContext(user.getId(), interests, null, limit));
        validate(result, limit);
        List<RecommendedContentResponse> responses = IntStream.range(0, result.size())
                .mapToObj(index -> toResponse(index, result.get(index))).toList();
        return new ContentRecommendationResponse(responses);
    }

    private void validate(List<RecommendedContent> result, int limit) {
        if (result == null || result.isEmpty() || result.size() > limit) {
            throw new BusinessException(ErrorCode.CONTENT_RECOMMENDATION_FAILED);
        }
        Set<String> urls = new HashSet<>();
        for (RecommendedContent content : result) {
            if (content == null || content.title() == null || content.title().isBlank()
                    || content.description() == null || content.description().isBlank()
                    || content.contentType() == null || content.matchedTags() == null) {
                throw new BusinessException(ErrorCode.CONTENT_RECOMMENDATION_FAILED);
            }
            if (!RecommendationUrlValidator.isSafe(content.url()) || !urls.add(content.url())) {
                throw new BusinessException(ErrorCode.INVALID_RECOMMENDATION_URL);
            }
        }
    }

    private RecommendedContentResponse toResponse(int index, RecommendedContent content) {
        return new RecommendedContentResponse("content-" + (index + 1), content.title(), content.description(),
                content.url(), content.contentType(), List.copyOf(content.matchedTags()));
    }
}
