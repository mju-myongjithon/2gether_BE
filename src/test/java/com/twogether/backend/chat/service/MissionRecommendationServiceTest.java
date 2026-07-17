package com.twogether.backend.chat.service;

import com.twogether.backend.chat.domain.ChatRoom;
import com.twogether.backend.chat.dto.request.ShareMissionRecommendationRequest;
import com.twogether.backend.chat.dto.response.ChatMessageResponse;
import com.twogether.backend.chat.recommendation.AiMissionRecommendationClient;
import com.twogether.backend.chat.recommendation.MissionDifficulty;
import com.twogether.backend.chat.recommendation.MissionRecommendationContext;
import com.twogether.backend.chat.recommendation.RecommendedMission;
import com.twogether.backend.chat.repository.ChatRoomMemberRepository;
import com.twogether.backend.chat.repository.ChatRoomRepository;
import com.twogether.backend.gathering.domain.Gathering;
import com.twogether.backend.gathering.domain.GatheringCategory;
import com.twogether.backend.gathering.repository.GatheringRepository;
import com.twogether.backend.gathering.repository.GatheringTagRepository;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.tag.repository.UserTagRepository;
import com.twogether.backend.user.domain.User;
import com.twogether.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MissionRecommendationServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final ChatRoomRepository roomRepository = mock(ChatRoomRepository.class);
    private final ChatRoomMemberRepository memberRepository = mock(ChatRoomMemberRepository.class);
    private final GatheringRepository gatheringRepository = mock(GatheringRepository.class);
    private final GatheringTagRepository gatheringTagRepository = mock(GatheringTagRepository.class);
    private final UserTagRepository userTagRepository = mock(UserTagRepository.class);
    private final AiMissionRecommendationClient client = mock(AiMissionRecommendationClient.class);
    private final ChatMessageService messageService = mock(ChatMessageService.class);
    private MissionRecommendationService service;
    private User user;

    @BeforeEach
    void setUp() {
        service = new MissionRecommendationService(userRepository, roomRepository, memberRepository,
                gatheringRepository, gatheringTagRepository, userTagRepository, client, messageService);
        user = mock(User.class);
        when(user.getId()).thenReturn(7L);
        when(userRepository.findByAuthUserId("subject")).thenReturn(Optional.of(user));
    }

    @Test
    void recommendsForCurrentParticipantAndAssignsSequentialIds() {
        ChatRoom room = ChatRoom.group(9L, "스터디방");
        Gathering gathering = mock(Gathering.class);
        when(gathering.getTitle()).thenReturn("알고리즘 스터디");
        when(gathering.getContent()).thenReturn("함께 공부합니다");
        when(gathering.getCategory()).thenReturn(GatheringCategory.STUDY);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(memberRepository.existsByChatRoomIdAndUserIdAndLeftAtIsNull(1L, 7L)).thenReturn(true);
        when(gatheringRepository.findById(9L)).thenReturn(Optional.of(gathering));
        when(gatheringTagRepository.findTagNamesByGatheringIds(List.of(9L))).thenReturn(List.of());
        when(memberRepository.findAllByChatRoomIdAndLeftAtIsNull(1L)).thenReturn(List.of());
        when(client.recommend(org.mockito.ArgumentMatchers.any())).thenReturn(List.of(
                new RecommendedMission("목표 공유", "목표를 하나씩 공유해보세요.", MissionDifficulty.EASY),
                new RecommendedMission("역할 정하기", "역할을 하나씩 정해보세요.", MissionDifficulty.NORMAL)));

        var result = service.recommend("subject", 1L);

        assertThat(result.missions()).extracting("missionId").containsExactly("mission-1", "mission-2");
        assertThat(result.missions()).extracting("difficulty").containsExactly("EASY", "NORMAL");
        ArgumentCaptor<MissionRecommendationContext> context = ArgumentCaptor.forClass(MissionRecommendationContext.class);
        verify(client).recommend(context.capture());
        assertThat(context.getValue().gatheringTitle()).isEqualTo("알고리즘 스터디");
        assertThat(context.getValue().participantInterestTags()).isEmpty();
    }

    @Test
    void rejectsNonCurrentParticipant() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(ChatRoom.group(9L, "방")));

        assertThatThrownBy(() -> service.recommend("subject", 1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.CHAT_ROOM_ACCESS_DENIED);
    }

    @Test
    void rejectsRoomWithoutGathering() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(ChatRoom.direct("1:1")));
        when(memberRepository.existsByChatRoomIdAndUserIdAndLeftAtIsNull(1L, 7L)).thenReturn(true);

        assertThatThrownBy(() -> service.recommend("subject", 1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.MISSION_RECOMMENDATION_NOT_AVAILABLE);
    }

    @Test
    void rejectsMissingRoomAndMissingGathering() {
        assertThatThrownBy(() -> service.recommend("subject", 404L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.CHAT_ROOM_NOT_FOUND);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(ChatRoom.group(9L, "방")));
        when(memberRepository.existsByChatRoomIdAndUserIdAndLeftAtIsNull(1L, 7L)).thenReturn(true);
        assertThatThrownBy(() -> service.recommend("subject", 1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.GATHERING_NOT_FOUND);
    }

    @Test
    void rejectsEmptyOrTooManyRecommendations() {
        prepareValidRoom();
        when(client.recommend(org.mockito.ArgumentMatchers.any())).thenReturn(List.of());
        assertThatThrownBy(() -> service.recommend("subject", 1L))
                .extracting("errorCode").isEqualTo(ErrorCode.MISSION_RECOMMENDATION_FAILED);

        when(client.recommend(org.mockito.ArgumentMatchers.any())).thenReturn(List.of(
                mission(), mission(), mission(), mission()));
        assertThatThrownBy(() -> service.recommend("subject", 1L))
                .extracting("errorCode").isEqualTo(ErrorCode.MISSION_RECOMMENDATION_FAILED);
    }

    @Test
    void sharesThroughExistingCardPipeline() {
        ShareMissionRecommendationRequest request = new ShareMissionRecommendationRequest("미션", "함께 실행해보세요.");
        ChatMessageResponse expected = mock(ChatMessageResponse.class);
        when(messageService.sendCard("subject", 1L, "MISSION_RECOMMENDATION", "미션", "함께 실행해보세요."))
                .thenReturn(expected);

        assertThat(service.share("subject", 1L, request)).isSameAs(expected);
        verify(messageService).sendCard("subject", 1L, "MISSION_RECOMMENDATION", "미션", "함께 실행해보세요.");
    }

    private void prepareValidRoom() {
        Gathering gathering = mock(Gathering.class);
        when(gathering.getCategory()).thenReturn(GatheringCategory.STUDY);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(ChatRoom.group(9L, "방")));
        when(memberRepository.existsByChatRoomIdAndUserIdAndLeftAtIsNull(1L, 7L)).thenReturn(true);
        when(gatheringRepository.findById(9L)).thenReturn(Optional.of(gathering));
        when(gatheringTagRepository.findTagNamesByGatheringIds(List.of(9L))).thenReturn(List.of());
        when(memberRepository.findAllByChatRoomIdAndLeftAtIsNull(1L)).thenReturn(List.of());
    }

    private RecommendedMission mission() {
        return new RecommendedMission("미션", "실행해보세요.", MissionDifficulty.EASY);
    }
}
