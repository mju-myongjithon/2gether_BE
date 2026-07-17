package com.twogether.backend.contentrecommendation.service;

import com.twogether.backend.contentrecommendation.client.AiContentRecommendationClient;
import com.twogether.backend.contentrecommendation.client.RecommendedContent;
import com.twogether.backend.contentrecommendation.domain.ContentType;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.tag.domain.Tag;
import com.twogether.backend.tag.domain.UserTag;
import com.twogether.backend.tag.repository.UserTagRepository;
import com.twogether.backend.user.domain.User;
import com.twogether.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ContentRecommendationServiceTest {

    private UserRepository userRepository;
    private UserTagRepository userTagRepository;
    private AiContentRecommendationClient client;
    private ContentRecommendationService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userTagRepository = mock(UserTagRepository.class);
        client = mock(AiContentRecommendationClient.class);
        service = new ContentRecommendationService(userRepository, userTagRepository, client);
    }

    @Test
    void 관심사로_추천하고_contentId를_순서대로_부여한다() {
        prepareUser(List.of("백엔드", "백엔드", "Spring"));
        when(client.recommend(any())).thenReturn(List.of(
                content("https://spring.io/guides", ContentType.BLOG, List.of("백엔드", "Spring")),
                content("https://github.com/features", ContentType.ARTICLE, List.of("백엔드"))));

        var response = service.recommend("auth-id", 6);

        assertThat(response.recommendations()).extracting(value -> value.contentId())
                .containsExactly("content-1", "content-2");
        assertThat(response.recommendations().get(0).matchedTags()).containsExactly("백엔드", "Spring");
        verify(userRepository, never()).save(any());
        verify(userTagRepository, never()).save(any());
    }

    @Test
    void 관심사가_없어도_추천한다() {
        prepareUser(List.of());
        when(client.recommend(any())).thenReturn(List.of(content("https://spring.io", ContentType.BLOG, List.of())));
        assertThat(service.recommend("auth-id", 6).recommendations()).hasSize(1);
    }

    @Test
    void 존재하지_않는_사용자는_실패한다() {
        when(userRepository.findByAuthUserId("missing")).thenReturn(Optional.empty());
        assertError(() -> service.recommend("missing", 6), ErrorCode.USER_NOT_FOUND);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 11})
    void 범위를_벗어난_limit은_실패한다(int limit) {
        assertError(() -> service.recommend("auth-id", limit), ErrorCode.INVALID_REQUEST);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10})
    void 경계_limit은_성공한다(int limit) {
        prepareUser(List.of());
        when(client.recommend(any())).thenReturn(List.of(content("https://spring.io", ContentType.BLOG, List.of())));
        assertThat(service.recommend("auth-id", limit).recommendations()).hasSize(1);
    }

    @Test
    void null과_빈_결과와_limit_초과를_거부한다() {
        prepareUser(List.of());
        when(client.recommend(any())).thenReturn(null);
        assertError(() -> service.recommend("auth-id", 1), ErrorCode.CONTENT_RECOMMENDATION_FAILED);
        when(client.recommend(any())).thenReturn(List.of());
        assertError(() -> service.recommend("auth-id", 1), ErrorCode.CONTENT_RECOMMENDATION_FAILED);
        when(client.recommend(any())).thenReturn(List.of(
                content("https://spring.io", ContentType.BLOG, List.of()),
                content("https://github.com", ContentType.ARTICLE, List.of())));
        assertError(() -> service.recommend("auth-id", 1), ErrorCode.CONTENT_RECOMMENDATION_FAILED);
    }

    @Test
    void 필수_필드가_없는_결과를_거부한다() {
        prepareUser(List.of());
        when(client.recommend(any())).thenReturn(List.of(new RecommendedContent(null, "설명", "https://spring.io", ContentType.BLOG, List.of())));
        assertError(() -> service.recommend("auth-id", 1), ErrorCode.CONTENT_RECOMMENDATION_FAILED);
        when(client.recommend(any())).thenReturn(List.of(new RecommendedContent("제목", null, "https://spring.io", ContentType.BLOG, List.of())));
        assertError(() -> service.recommend("auth-id", 1), ErrorCode.CONTENT_RECOMMENDATION_FAILED);
    }

    @ParameterizedTest
    @ValueSource(strings = {"javascript:alert(1)", "https://localhost/path", "https://127.0.0.1/path", "http://example.com"})
    void 안전하지_않은_URL을_거부한다(String url) {
        prepareUser(List.of());
        when(client.recommend(any())).thenReturn(List.of(content(url, ContentType.BLOG, List.of())));
        assertError(() -> service.recommend("auth-id", 1), ErrorCode.INVALID_RECOMMENDATION_URL);
    }

    @Test
    void 중복_URL을_거부한다() {
        prepareUser(List.of());
        when(client.recommend(any())).thenReturn(List.of(
                content("https://spring.io", ContentType.BLOG, List.of()),
                content("https://spring.io", ContentType.ARTICLE, List.of())));
        assertError(() -> service.recommend("auth-id", 2), ErrorCode.INVALID_RECOMMENDATION_URL);
    }

    private void prepareUser(List<String> tagNames) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(userRepository.findByAuthUserId("auth-id")).thenReturn(Optional.of(user));
        List<UserTag> tags = tagNames.stream().map(name -> {
            Tag tag = mock(Tag.class);
            when(tag.getName()).thenReturn(name);
            UserTag userTag = mock(UserTag.class);
            when(userTag.getTag()).thenReturn(tag);
            return userTag;
        }).toList();
        when(userTagRepository.findAllByUser_IdOrderByTag_IdAsc(1L)).thenReturn(tags);
    }

    private RecommendedContent content(String url, ContentType type, List<String> matchedTags) {
        return new RecommendedContent("제목", "설명", url, type, matchedTags);
    }

    private void assertError(Runnable action, ErrorCode errorCode) {
        assertThatThrownBy(action::run).isInstanceOfSatisfying(BusinessException.class,
                exception -> assertThat(exception.getErrorCode()).isEqualTo(errorCode));
    }
}
