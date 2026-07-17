package com.twogether.backend.chat.service;

import com.twogether.backend.chat.domain.ChatRoom;
import com.twogether.backend.chat.domain.ChatRoomMember;
import com.twogether.backend.chat.dto.request.ShareMissionRecommendationRequest;
import com.twogether.backend.chat.dto.response.ChatMessageResponse;
import com.twogether.backend.chat.dto.response.MissionRecommendationResponse;
import com.twogether.backend.chat.dto.response.RecommendedMissionResponse;
import com.twogether.backend.chat.recommendation.AiMissionRecommendationClient;
import com.twogether.backend.chat.recommendation.MissionRecommendationContext;
import com.twogether.backend.chat.recommendation.RecommendedMission;
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
import java.util.stream.IntStream;

@Service
@Transactional(readOnly = true)
public class MissionRecommendationService {

    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final GatheringRepository gatheringRepository;
    private final GatheringTagRepository gatheringTagRepository;
    private final UserTagRepository userTagRepository;
    private final AiMissionRecommendationClient recommendationClient;
    private final ChatMessageService chatMessageService;

    public MissionRecommendationService(UserRepository userRepository,
                                        ChatRoomRepository chatRoomRepository,
                                        ChatRoomMemberRepository chatRoomMemberRepository,
                                        GatheringRepository gatheringRepository,
                                        GatheringTagRepository gatheringTagRepository,
                                        UserTagRepository userTagRepository,
                                        AiMissionRecommendationClient recommendationClient,
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

    public MissionRecommendationResponse recommend(String authUserId, Long chatRoomId) {
        User user = userRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        ChatRoom room = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));
        verifyParticipant(chatRoomId, user.getId());

        Long gatheringId = room.getGatheringId();
        if (gatheringId == null) {
            throw new BusinessException(ErrorCode.MISSION_RECOMMENDATION_NOT_AVAILABLE);
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

        List<RecommendedMission> recommended = recommendationClient.recommend(new MissionRecommendationContext(
                chatRoomId, gatheringId, gathering.getTitle(), gathering.getContent(), gathering.getCategory(),
                gatheringTags, interests));
        if (recommended == null || recommended.isEmpty() || recommended.size() > 3) {
            throw new BusinessException(ErrorCode.MISSION_RECOMMENDATION_FAILED);
        }

        List<RecommendedMissionResponse> missions = IntStream.range(0, recommended.size())
                .mapToObj(i -> toResponse(i, recommended.get(i)))
                .toList();
        return new MissionRecommendationResponse(chatRoomId, missions);
    }

    @Transactional
    public ChatMessageResponse share(String authUserId, Long chatRoomId, ShareMissionRecommendationRequest request) {
        return chatMessageService.sendCard(
                authUserId, chatRoomId, "MISSION_RECOMMENDATION", request.title(), request.content());
    }

    private RecommendedMissionResponse toResponse(int index, RecommendedMission mission) {
        if (mission == null || mission.title() == null || mission.content() == null || mission.difficulty() == null) {
            throw new BusinessException(ErrorCode.MISSION_RECOMMENDATION_FAILED);
        }
        return new RecommendedMissionResponse(
                "mission-" + (index + 1), mission.title(), mission.content(), mission.difficulty().name());
    }

    private void verifyParticipant(Long chatRoomId, Long userId) {
        if (!chatRoomMemberRepository.existsByChatRoomIdAndUserIdAndLeftAtIsNull(chatRoomId, userId)) {
            throw new BusinessException(ErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }
    }
}
