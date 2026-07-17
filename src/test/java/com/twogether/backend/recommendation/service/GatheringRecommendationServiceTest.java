package com.twogether.backend.recommendation.service;

import com.twogether.backend.gathering.domain.Gathering;
import com.twogether.backend.gathering.domain.GatheringCategory;
import com.twogether.backend.gathering.domain.GatheringTag;
import com.twogether.backend.gathering.repository.GatheringMemberRepository;
import com.twogether.backend.gathering.repository.GatheringRepository;
import com.twogether.backend.gathering.repository.GatheringTagRepository;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.recommendation.client.MockAiRecommendationClient;
import com.twogether.backend.recommendation.dto.request.AiGatheringInfo;
import com.twogether.backend.recommendation.dto.request.AiGatheringRecommendationRequest;
import com.twogether.backend.recommendation.dto.response.AiRecommendationResponse;
import com.twogether.backend.tag.domain.Tag;
import com.twogether.backend.tag.domain.TagType;
import com.twogether.backend.tag.domain.UserTag;
import com.twogether.backend.tag.repository.TagRepository;
import com.twogether.backend.tag.repository.UserTagRepository;
import com.twogether.backend.user.domain.User;
import com.twogether.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GatheringRecommendationServiceTest {

    private static final Long GATHERING_ID = 1L;

    private GatheringRecommendationService recommendationService;
    private GatheringTagRepository gatheringTagRepository;
    private TagRepository tagRepository;
    private UserRepository userRepository;
    private UserTagRepository userTagRepository;

    @BeforeEach
    void setUp() {
        GatheringRepository gatheringRepository = mock(GatheringRepository.class);
        gatheringTagRepository = mock(GatheringTagRepository.class);
        tagRepository = mock(TagRepository.class);
        userRepository = mock(UserRepository.class);
        GatheringMemberRepository gatheringMemberRepository =
                mock(GatheringMemberRepository.class);
        userTagRepository = mock(UserTagRepository.class);

        Gathering gathering = mock(Gathering.class);
        when(gathering.getId()).thenReturn(GATHERING_ID);
        when(gathering.getTitle()).thenReturn("해커톤 팀원 모집");
        when(gathering.getContent()).thenReturn("Spring Boot 기반 서비스를 개발합니다.");
        when(gathering.getCategory()).thenReturn(GatheringCategory.HACKATHON);
        when(gathering.getMaxMembers()).thenReturn((short) 6);
        when(gathering.getCurrentMembers()).thenReturn((short) 2);
        when(gathering.isFusionEnabled()).thenReturn(true);

        when(gatheringRepository.findById(GATHERING_ID))
                .thenReturn(Optional.of(gathering));
        when(gatheringTagRepository.findAllByGathering_Id(GATHERING_ID))
                .thenReturn(List.of());
        when(tagRepository.findAllById(List.of())).thenReturn(List.of());
        when(gatheringMemberRepository.findByGatheringIdWithUser(GATHERING_ID))
                .thenReturn(List.of());
        when(userRepository.findAll()).thenReturn(List.of());

        recommendationService = new GatheringRecommendationService(
                new MockAiRecommendationClient(),
                gatheringRepository,
                gatheringTagRepository,
                tagRepository,
                userRepository,
                gatheringMemberRepository,
                userTagRepository
        );
    }

    @Test
    void DB에서_조회한_태그_일치도가_높은_후보부터_추천한다() {
        Tag springTag = tag(1L, "Spring Boot");
        Tag javaTag = tag(2L, "Java");
        GatheringTag springGatheringTag = gatheringTag(1L);
        GatheringTag javaGatheringTag = gatheringTag(2L);
        User firstUser = candidateUser(10L, "후보1");
        User secondUser = candidateUser(20L, "후보2");

        when(gatheringTagRepository.findAllByGathering_Id(GATHERING_ID))
                .thenReturn(List.of(springGatheringTag, javaGatheringTag));
        when(tagRepository.findAllById(List.of(1L, 2L)))
                .thenReturn(List.of(springTag, javaTag));
        when(userRepository.findAll()).thenReturn(List.of(firstUser, secondUser));
        UserTag firstUserSpringTag = userTag(firstUser, springTag);
        UserTag secondUserSpringTag = userTag(secondUser, springTag);
        UserTag secondUserJavaTag = userTag(secondUser, javaTag);
        when(userTagRepository.findAllByUserIdsWithTag(List.of(10L, 20L)))
                .thenReturn(List.of(
                        firstUserSpringTag,
                        secondUserSpringTag,
                        secondUserJavaTag
                ));

        AiRecommendationResponse response = recommendationService.recommend(
                GATHERING_ID,
                request(1)
        );

        assertThat(response.recommendations()).hasSize(1);
        assertThat(response.recommendations().get(0).userId()).isEqualTo(20L);
        assertThat(response.recommendations().get(0).score()).isEqualTo(80);
    }

    @Test
    void DB_후보가_없으면_예외가_발생한다() {
        assertThatThrownBy(() -> recommendationService.recommend(
                GATHERING_ID,
                request(1)
        ))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(
                        ((BusinessException) exception).getErrorCode()
                ).isEqualTo(ErrorCode.RECOMMENDATION_CANDIDATE_EMPTY));
    }

    @Test
    void 추천_인원이_DB_후보_수보다_크면_예외가_발생한다() {
        User candidate = candidateUser(10L, "후보1");
        when(userRepository.findAll()).thenReturn(List.of(candidate));
        when(userTagRepository.findAllByUserIdsWithTag(List.of(10L)))
                .thenReturn(List.of());

        assertThatThrownBy(() -> recommendationService.recommend(
                GATHERING_ID,
                request(2)
        ))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(
                        ((BusinessException) exception).getErrorCode()
                ).isEqualTo(ErrorCode.INVALID_RECOMMENDATION_COUNT));
    }

    private AiGatheringRecommendationRequest request(int recommendationCount) {
        AiGatheringInfo gatheringInfo = new AiGatheringInfo(
                GATHERING_ID,
                "요청 본문의 모임 정보",
                "요청 본문의 내용",
                "HACKATHON",
                6,
                2,
                true,
                List.of()
        );
        return new AiGatheringRecommendationRequest(
                gatheringInfo,
                List.of(),
                recommendationCount
        );
    }

    private User candidateUser(Long id, String nickname) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(id);
        when(user.getNickname()).thenReturn(nickname);
        when(user.getDepartmentId()).thenReturn(1L);
        when(user.getPreferredRegion()).thenReturn("자연캠");
        when(user.getIntroduction()).thenReturn("백엔드 개발에 관심이 있습니다.");
        when(user.isProfileCompleted()).thenReturn(true);
        when(user.isEmailVerified()).thenReturn(true);
        return user;
    }

    private GatheringTag gatheringTag(Long tagId) {
        GatheringTag gatheringTag = mock(GatheringTag.class);
        when(gatheringTag.getTagId()).thenReturn(tagId);
        return gatheringTag;
    }

    private Tag tag(Long id, String name) {
        Tag tag = mock(Tag.class);
        when(tag.getId()).thenReturn(id);
        when(tag.getName()).thenReturn(name);
        when(tag.getType()).thenReturn(TagType.SKILL);
        return tag;
    }

    private UserTag userTag(User user, Tag tag) {
        UserTag userTag = mock(UserTag.class);
        when(userTag.getUser()).thenReturn(user);
        when(userTag.getTag()).thenReturn(tag);
        return userTag;
    }
}
