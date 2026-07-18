package com.twogether.backend.chat.recommendation;

import com.twogether.backend.gathering.domain.GatheringCategory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnExpression("'${app.ai.chat-recommendation.mission-mode:mock}' == 'mock' or '${app.ai.chat-recommendation.mission-mode:mock}' == 'demo'")
public class MockAiMissionRecommendationClient implements AiMissionRecommendationClient {

    @Override
    public List<RecommendedMission> recommend(MissionRecommendationContext context) {
        List<RecommendedMission> missions = new ArrayList<>(categoryMissions(context.category()));
        String interest = firstTag(context.participantInterestTags(), context.gatheringTags());
        if (interest != null) {
            missions.set(2, mission(
                    interest + " 활동 정하기",
                    interest + " 관심사를 모임 목적과 연결한 짧은 활동을 하나씩 제안하고, 바로 해볼 활동 하나를 투표로 정해보세요."
            ));
        }
        return List.copyOf(missions.subList(0, Math.min(3, missions.size())));
    }

    private List<RecommendedMission> categoryMissions(GatheringCategory category) {
        if (category == null) return defaultMissions();
        return switch (category) {
            case HACKATHON, PROJECT -> List.of(
                    mission("문제 후보 3개 작성하기", "참여자들이 해결하고 싶은 문제를 하나씩 제안하고, 함께 다룰 문제 후보 3개를 작성해보세요."),
                    mission("강점으로 역할 나누기", "각자 자신의 강점을 짧게 소개하고, 그 강점을 살릴 수 있도록 역할을 나눠보세요."),
                    mission("핵심 기능 투표하기", "짧은 시간에 구현할 핵심 기능을 하나씩 제안한 뒤, 투표로 한 가지를 정해보세요."));
            case STUDY -> List.of(
                    mission("이번 주 목표 공유하기", "각자 이번 주에 달성할 학습 목표를 하나씩 공유하고 서로 확인해보세요."),
                    mission("개념 하나 설명하기", "각자 어려웠던 개념을 하나 고르고, 알고 있는 내용을 서로에게 짧게 설명해보세요."),
                    mission("작은 학습 목표 정하기", "다음 모임 전까지 달성할 수 있는 작은 학습 목표를 하나씩 정해보세요."));
            case NETWORKING -> List.of(
                    mission("공통점 3가지 찾기", "참여자들이 서로 소개를 나누고 모두의 공통점 3가지를 찾아보세요."),
                    mission("관심 분야 소개하기", "각자 전공과 관심 분야를 짧게 소개하고 연결되는 분야를 하나 찾아보세요."),
                    mission("추천 장소 공유하기", "인문캠과 자연캠에서 추천하고 싶은 장소를 하나씩 공유하고 함께 갈 곳을 골라보세요."));
            case HOBBY -> List.of(
                    mission("최애 콘텐츠 소개하기", "각자 가장 좋아하는 콘텐츠나 활동을 하나씩 소개하고 함께 즐길 후보를 골라보세요."),
                    mission("활동 후보 3개 정하기", "함께 해보고 싶은 부담 없는 활동을 제안하고 후보 3개로 정리해보세요."),
                    mission("다음 활동 투표하기", "후보 중 다음에 함께 할 활동 하나를 투표로 결정해보세요."));
        };
    }

    private List<RecommendedMission> defaultMissions() {
        return List.of(
                mission("공통점 3가지 찾기", "참여자들이 대화를 나누며 서로의 공통점 3가지를 찾아보세요."),
                mission("기대 활동 공유하기", "각자 이번 모임에서 기대하는 활동을 하나씩 공유해보세요."),
                mission("작은 활동 정하기", "다음 모임에서 함께 할 수 있는 작은 활동을 하나씩 제안하고 한 가지를 정해보세요."));
    }

    private RecommendedMission mission(String title, String content) {
        return new RecommendedMission(title, content, MissionDifficulty.EASY);
    }

    private String firstTag(List<String> interests, List<String> gatheringTags) {
        if (interests != null && !interests.isEmpty()) return interests.get(0);
        if (gatheringTags != null && !gatheringTags.isEmpty()) return gatheringTags.get(0);
        return null;
    }
}
