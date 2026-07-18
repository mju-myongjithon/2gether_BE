package com.twogether.backend.chat.recommendation;

import com.twogether.backend.gathering.domain.GatheringCategory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnExpression("'${app.ai.chat-recommendation.topic-mode:mock}' == 'mock' or '${app.ai.chat-recommendation.topic-mode:mock}' == 'demo'")
public class MockAiTopicRecommendationClient implements AiTopicRecommendationClient {

    @Override
    public List<RecommendedTopic> recommend(TopicRecommendationContext context) {
        List<RecommendedTopic> topics = new ArrayList<>(categoryTopics(context.category()));
        String tag = firstTag(context.participantInterestTags(), context.gatheringTags());
        if (tag != null) {
            topics.set(2, new RecommendedTopic(
                    tag + "로 함께할 아이디어",
                    tag + "에 대한 관심을 모임 목표와 연결해 보세요. 각자가 시도해 보고 싶은 활동을 한 가지씩 나눠 보세요."
            ));
        }
        return List.copyOf(topics.subList(0, Math.min(3, topics.size())));
    }

    private List<RecommendedTopic> categoryTopics(GatheringCategory category) {
        return switch (category) {
            case HACKATHON, PROJECT -> List.of(
                    topic("해결하고 싶은 문제", "학교생활이나 일상에서 불편했던 문제를 나눠 보세요. 이번 모임에서 해결할 수 있는 작은 범위를 함께 정해 보세요."),
                    topic("각자의 역할과 강점", "기획, 디자인, 개발 등 자신 있는 부분을 소개해 보세요. 서로의 강점을 살릴 역할도 가볍게 맞춰 보세요."),
                    topic("꼭 구현하고 싶은 기능", "결과물에 반드시 담고 싶은 핵심 기능을 하나씩 제안해 보세요. 우선순위를 함께 정해 보세요."));
            case STUDY -> List.of(
                    topic("이번 스터디의 목표", "이번 모임을 통해 얻고 싶은 결과를 한 가지씩 말해 보세요. 공통 목표를 짧게 정리해 보세요."),
                    topic("서로 알려줄 수 있는 주제", "각자가 익숙한 내용과 배우고 싶은 내용을 나눠 보세요. 서로 도울 수 있는 짝을 찾아보세요."),
                    topic("진행 방식과 역할 분담", "선호하는 학습 방식과 가능한 준비 시간을 공유해 보세요. 부담 없는 진행 규칙을 정해 보세요."));
            case NETWORKING -> List.of(
                    topic("서로의 전공과 관심 분야", "현재 배우거나 탐구 중인 분야를 소개해 보세요. 서로 연결되는 관심사를 찾아보세요."),
                    topic("캠퍼스 생활 추천", "최근 발견한 유용한 장소나 활동을 추천해 보세요. 함께 해보고 싶은 것도 골라 보세요."),
                    topic("함께 해보고 싶은 교류", "앞으로 같이 해보고 싶은 프로젝트나 활동을 이야기해 보세요. 다음 만남으로 이어질 작은 약속을 정해 보세요."));
            case HOBBY -> List.of(
                    topic("좋아하는 활동 방식", "이 취미를 즐길 때 선호하는 방식과 경험을 나눠 보세요. 함께 맞출 수 있는 활동 방식을 찾아보세요."),
                    topic("가능한 일정과 장소", "편한 시간대와 선호 장소를 공유해 보세요. 모두가 참여하기 쉬운 첫 일정을 정해 보세요."),
                    topic("이번 모임의 작은 목표", "이번 모임에서 해보고 싶은 것을 한 가지씩 말해 보세요. 함께 달성할 작은 목표를 골라 보세요."));
        };
    }

    private RecommendedTopic topic(String title, String content) {
        return new RecommendedTopic(title, content);
    }

    private String firstTag(List<String> interests, List<String> gatheringTags) {
        if (interests != null && !interests.isEmpty()) return interests.get(0);
        if (gatheringTags != null && !gatheringTags.isEmpty()) return gatheringTags.get(0);
        return null;
    }
}
