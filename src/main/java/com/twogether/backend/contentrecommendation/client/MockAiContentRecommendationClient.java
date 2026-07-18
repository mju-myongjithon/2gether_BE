package com.twogether.backend.contentrecommendation.client;

import com.twogether.backend.contentrecommendation.domain.ContentType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
@ConditionalOnExpression("'${app.ai.content-recommendation.mode:mock}' == 'mock' or '${app.ai.content-recommendation.mode:mock}' == 'demo'")
public class MockAiContentRecommendationClient implements AiContentRecommendationClient {

    private static final List<Candidate> CANDIDATES = List.of(
            candidate("Spring Boot 공식 가이드", "Spring Boot로 애플리케이션을 만드는 방법을 살펴보는 공식 학습 자료입니다.", "https://spring.io/guides", ContentType.BLOG, "개발", "백엔드", "Spring", "스프링"),
            candidate("GitHub 협업 기능", "프로젝트 협업에 활용할 수 있는 GitHub의 공식 기능과 문서를 확인합니다.", "https://github.com/features", ContentType.ARTICLE, "개발", "협업", "프로젝트"),
            candidate("국내 해커톤 정보", "공모전과 해커톤 일정을 탐색할 수 있는 공식 정보 서비스입니다.", "https://www.wevity.com/", ContentType.EVENT, "개발", "해커톤", "공모전"),
            candidate("국민체육진흥공단", "건강한 운동과 생활체육 관련 정보를 확인할 수 있습니다.", "https://www.kspo.or.kr/", ContentType.ACTIVITY, "운동", "체육", "러닝"),
            candidate("서울시 생활체육", "가까운 생활체육 프로그램과 시설 정보를 찾아볼 수 있습니다.", "https://sports.seoul.go.kr/", ContentType.PLACE, "운동", "체육", "러닝"),
            candidate("국립중앙박물관 전시", "전시와 문화 프로그램 정보를 공식 홈페이지에서 확인합니다.", "https://www.museum.go.kr/", ContentType.EVENT, "문화", "전시", "공연"),
            candidate("서울문화포털", "공연, 전시, 문화행사와 문화 공간 정보를 찾아볼 수 있습니다.", "https://culture.seoul.go.kr/", ContentType.PLACE, "문화", "전시", "공연"),
            candidate("BoardGameGeek", "다양한 보드게임 정보와 입문 자료를 탐색할 수 있습니다.", "https://boardgamegeek.com/", ContentType.BLOG, "보드게임", "취미"),
            candidate("서울시 공공서비스예약", "체육, 문화, 교육 등 다양한 활동을 찾아 예약할 수 있습니다.", "https://yeyak.seoul.go.kr/", ContentType.ACTIVITY, "취미", "활동", "문화", "운동")
    );

    @Override
    public List<RecommendedContent> recommend(ContentRecommendationContext context) {
        List<String> interests = context.interestTags() == null ? List.of() : context.interestTags();
        int limit = context.limit();
        return CANDIDATES.stream()
                .sorted(Comparator.comparingInt((Candidate value) -> matched(value, interests).size()).reversed())
                .limit(limit)
                .map(value -> new RecommendedContent(value.title(), value.description(), value.url(),
                        value.contentType(), matched(value, interests)))
                .toList();
    }

    private List<String> matched(Candidate candidate, List<String> interests) {
        return interests.stream().filter(interest -> candidate.tags().stream()
                        .anyMatch(tag -> tag.toLowerCase(Locale.ROOT).equals(interest.toLowerCase(Locale.ROOT))))
                .distinct().toList();
    }

    private static Candidate candidate(String title, String description, String url, ContentType type, String... tags) {
        return new Candidate(title, description, url, type, Set.of(tags));
    }

    private record Candidate(String title, String description, String url, ContentType contentType, Set<String> tags) {
    }
}
