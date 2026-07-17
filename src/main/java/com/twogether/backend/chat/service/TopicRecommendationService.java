package com.twogether.backend.chat.service;

import com.twogether.backend.chat.domain.ChatRoom;
import com.twogether.backend.chat.domain.ChatRoomMember;
import com.twogether.backend.chat.dto.request.ShareTopicRecommendationRequest;
import com.twogether.backend.chat.dto.response.ChatMessageResponse;
import com.twogether.backend.chat.dto.response.RecommendedTopicResponse;
import com.twogether.backend.chat.dto.response.TopicRecommendationResponse;
import com.twogether.backend.chat.recommendation.AiTopicRecommendationClient;
import com.twogether.backend.chat.recommendation.RecommendedTopic;
import com.twogether.backend.chat.recommendation.TopicRecommendationContext;
import com.twogether.backend.chat.repository.ChatRoomMemberRepository;
import com.twogether.backend.chat.repository.ChatRoomRepository;
import com.twogether.backend.gathering.domain.Gathering;
import com.twogether.backend.gathering.repository.GatheringRepository;
import com.twogether.backend.gathering.repository.GatheringTagRepository;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.tag.repository.UserTagRepository;
import com.twogether.backend.user.domain.User;
import com.twogether.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class TopicRecommendationService {

    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final GatheringRepository gatheringRepository;
    private final GatheringTagRepository gatheringTagRepository;
    private final UserTagRepository userTagRepository;
    private final AiTopicRecommendationClient recommendationClient;
    private final ChatMessageService chatMessageService;

    public TopicRecommendationService(UserRepository userRepository,
                                      ChatRoomRepository chatRoomRepository,
                                      ChatRoomMemberRepository chatRoomMemberRepository,
                                      GatheringRepository gatheringRepository,
                                      GatheringTagRepository gatheringTagRepository,
                                      UserTagRepository userTagRepository,
                                      AiTopicRecommendationClient recommendationClient,
                                      ChatMessageService chatMessageService) {
        this.userRepository = userRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.chatRoomMemberRepository = chatRoomMemberRepository;
        this.gatheringRepository = gatheringRepository;
        this.gatheringTagRepository = gatheringTagRepository;
        this.userTagRepository = userTagRepository;
        this.recommendationClient = recommendationClient;
        this.chatMessageService = chatMessageService;
    }

    public TopicRecommendationResponse recommend(String authUserId, Long chatRoomId) {
        User user = findUser(authUserId);
        ChatRoom room = findRoom(chatRoomId);
        verifyParticipant(chatRoomId, user.getId());
        Long gatheringId = room.getGatheringId();
        if (gatheringId == null) {
            throw new BusinessException(ErrorCode.TOPIC_RECOMMENDATION_NOT_AVAILABLE);
        }
        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GATHERING_NOT_FOUND));

        List<String> gatheringTags = gatheringTagRepository.findTagNamesByGatheringIds(List.of(gatheringId))
                .stream().map(tag -> tag.getTagName()).distinct().toList();
        List<Long> participantIds = chatRoomMemberRepository.findAllByChatRoomIdAndLeftAtIsNull(chatRoomId)
                .stream().map(ChatRoomMember::getUser).map(User::getId).toList();
        List<String> interests = participantIds.isEmpty() ? List.of() : new LinkedHashSet<>(
                userTagRepository.findAllByUserIdsWithTag(participantIds).stream()
                        .map(userTag -> userTag.getTag().getName()).toList()).stream().toList();

        List<RecommendedTopic> recommended = recommendationClient.recommend(new TopicRecommendationContext(
                chatRoomId, gatheringId, gathering.getTitle(), gathering.getContent(), gathering.getCategory(),
                gatheringTags, interests));
        if (recommended == null || recommended.isEmpty() || recommended.size() > 3) {
            throw new BusinessException(ErrorCode.AI_RECOMMENDATION_FAILED);
        }
        List<RecommendedTopicResponse> topics = java.util.stream.IntStream.range(0, recommended.size())
                .mapToObj(i -> new RecommendedTopicResponse("topic-" + (i + 1), recommended.get(i).title(), recommended.get(i).content()))
                .toList();
        return new TopicRecommendationResponse(chatRoomId, topics);
    }

    @Transactional
    public ChatMessageResponse share(String authUserId, Long chatRoomId, ShareTopicRecommendationRequest request) {
        return chatMessageService.sendCard(authUserId, chatRoomId, request.title(), request.content());
    }

    private User findUser(String authUserId) {
        return userRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private ChatRoom findRoom(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));
    }

    private void verifyParticipant(Long chatRoomId, Long userId) {
        if (!chatRoomMemberRepository.existsByChatRoomIdAndUserIdAndLeftAtIsNull(chatRoomId, userId)) {
            throw new BusinessException(ErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }
    }
}
