package com.twogether.backend.chat.recommendation;

import com.twogether.backend.gathering.domain.GatheringCategory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MockAiMissionRecommendationClientTest {

    private final MockAiMissionRecommendationClient client = new MockAiMissionRecommendationClient();

    @Test
    void recommendsHackathonActions() {
        List<RecommendedMission> result = client.recommend(context(GatheringCategory.HACKATHON, List.of(), List.of()));

        assertThat(result).hasSizeBetween(1, 3);
        assertThat(result).extracting(RecommendedMission::title)
                .contains("문제 후보 3개 작성하기", "강점으로 역할 나누기");
        assertThat(result).allMatch(mission -> mission.difficulty() == MissionDifficulty.EASY);
    }

    @Test
    void recommendsStudyActions() {
        List<RecommendedMission> result = client.recommend(context(GatheringCategory.STUDY, List.of(), List.of()));

        assertThat(result).extracting(RecommendedMission::title)
                .contains("이번 주 목표 공유하기", "개념 하나 설명하기");
    }

    @Test
    void reflectsParticipantInterestBeforeGatheringTag() {
        List<RecommendedMission> result = client.recommend(
                context(GatheringCategory.HOBBY, List.of("영화"), List.of("러닝")));

        assertThat(result.get(2).title()).contains("러닝");
        assertThat(result.get(2).content()).contains("러닝");
        assertThat(result).noneMatch(mission -> mission.title().endsWith("?"));
    }

    @Test
    void worksWithoutAnyTags() {
        assertThat(client.recommend(context(GatheringCategory.NETWORKING, List.of(), List.of())))
                .hasSize(3)
                .allMatch(mission -> !mission.content().endsWith("?"));
    }

    private MissionRecommendationContext context(
            GatheringCategory category, List<String> gatheringTags, List<String> interests) {
        return new MissionRecommendationContext(
                1L, 2L, "모임", "소개", category, gatheringTags, interests);
    }
}
