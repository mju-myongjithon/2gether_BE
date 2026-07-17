package com.twogether.backend.contentrecommendation.client;

import com.twogether.backend.contentrecommendation.domain.ContentType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MockAiContentRecommendationClientTest {

    private final MockAiContentRecommendationClient client = new MockAiContentRecommendationClient();

    @Test
    void 관심사와_일치하는_콘텐츠를_우선하고_limit을_지킨다() {
        List<RecommendedContent> result = client.recommend(
                new ContentRecommendationContext(1L, List.of("백엔드", "Spring"), null, 3));

        assertThat(result).hasSize(3);
        assertThat(result.get(0).matchedTags()).contains("백엔드", "Spring");
        assertThat(result).extracting(RecommendedContent::url).doesNotHaveDuplicates();
        assertThat(result).extracting(RecommendedContent::contentType)
                .contains(ContentType.BLOG, ContentType.ARTICLE);
    }

    @Test
    void 관심사가_없어도_안전한_기본_추천을_반환한다() {
        List<RecommendedContent> result = client.recommend(
                new ContentRecommendationContext(1L, List.of(), null, 6));

        assertThat(result).hasSize(6);
        assertThat(result).allSatisfy(content -> {
            assertThat(content.url()).startsWith("https://");
            assertThat(content.matchedTags()).isEmpty();
        });
        assertThat(result).extracting(RecommendedContent::contentType).containsAnyOf(
                ContentType.BLOG, ContentType.ARTICLE, ContentType.EVENT, ContentType.PLACE, ContentType.ACTIVITY);
    }

    @Test
    void 운동과_문화와_보드게임_카테고리를_추천한다() {
        for (String interest : List.of("운동", "문화", "보드게임")) {
            List<RecommendedContent> result = client.recommend(
                    new ContentRecommendationContext(1L, List.of(interest), null, 1));
            assertThat(result.get(0).matchedTags()).containsExactly(interest);
        }
    }
}
