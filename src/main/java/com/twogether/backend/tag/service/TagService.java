package com.twogether.backend.tag.service;

import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.tag.domain.Tag;
import com.twogether.backend.tag.domain.TagType;
import com.twogether.backend.tag.domain.UserTag;
import com.twogether.backend.tag.dto.response.HobbyTagResponse;
import com.twogether.backend.tag.dto.response.SkillTagResponse;
import com.twogether.backend.tag.dto.response.UserTagsResponse;
import com.twogether.backend.tag.repository.TagRepository;
import com.twogether.backend.tag.repository.UserTagRepository;
import com.twogether.backend.user.domain.User;
import com.twogether.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;
    private final UserTagRepository userTagRepository;
    private final UserRepository userRepository;

    public TagService(
            TagRepository tagRepository,
            UserTagRepository userTagRepository,
            UserRepository userRepository
    ) {
        this.tagRepository = tagRepository;
        this.userTagRepository = userTagRepository;
        this.userRepository = userRepository;
    }

    /**
     * 선택 가능한 전체 취미 태그를 조회합니다.
     */
    public List<HobbyTagResponse> getHobbyTags() {
        return tagRepository
                .findAllByTypeOrderByIdAsc(TagType.HOBBY)
                .stream()
                .map(tag ->
                        new HobbyTagResponse(
                                tag.getId(),
                                tag.getName()
                        )
                )
                .toList();
    }

    /**
     * 선택 가능한 전체 스킬 태그를 조회합니다.
     */
    public List<SkillTagResponse> getSkillTags() {
        return tagRepository
                .findAllByTypeOrderByIdAsc(TagType.SKILL)
                .stream()
                .map(tag ->
                        new SkillTagResponse(
                                tag.getId(),
                                tag.getName()
                        )
                )
                .toList();
    }

    /**
     * 현재 로그인한 사용자의 취미 태그를
     * 요청으로 받은 태그 목록으로 전체 교체합니다.
     */
    @Transactional
    public void updateMyHobbyTags(
            String authUserId,
            List<Long> tagIds
    ) {
        replaceUserTags(
                authUserId,
                tagIds,
                TagType.HOBBY
        );
    }

    /**
     * 현재 로그인한 사용자의 스킬 태그를
     * 요청으로 받은 태그 목록으로 전체 교체합니다.
     */
    @Transactional
    public void updateMySkillTags(
            String authUserId,
            List<Long> tagIds
    ) {
        replaceUserTags(
                authUserId,
                tagIds,
                TagType.SKILL
        );
    }

    /**
     * 특정 사용자가 선택한 취미/스킬 태그를 조회합니다.
     */
    public UserTagsResponse getUserTags(
            Long userId
    ) {
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(
                    ErrorCode.USER_NOT_FOUND
            );
        }

        List<UserTag> userTags =
                userTagRepository
                        .findAllByUser_IdOrderByTag_IdAsc(
                                userId
                        );

        List<HobbyTagResponse> hobbyTags =
                userTags.stream()
                        .map(UserTag::getTag)
                        .filter(tag ->
                                tag.getType() == TagType.HOBBY
                        )
                        .map(tag ->
                                new HobbyTagResponse(
                                        tag.getId(),
                                        tag.getName()
                                )
                        )
                        .toList();

        List<SkillTagResponse> skillTags =
                userTags.stream()
                        .map(UserTag::getTag)
                        .filter(tag ->
                                tag.getType() == TagType.SKILL
                        )
                        .map(tag ->
                                new SkillTagResponse(
                                        tag.getId(),
                                        tag.getName()
                                )
                        )
                        .toList();

        return new UserTagsResponse(
                hobbyTags,
                skillTags
        );
    }

    /**
     * 특정 종류의 사용자 태그를 전체 삭제한 뒤
     * 요청받은 태그 목록으로 다시 저장합니다.
     */
    private void replaceUserTags(
            String authUserId,
            List<Long> tagIds,
            TagType expectedType
    ) {
        if (tagIds == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_TAG_REQUEST
            );
        }

        User user = userRepository
                .findByAuthUserId(authUserId)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );

        /*
         * 같은 태그 ID가 요청에 여러 번 들어와도
         * 한 번만 저장하도록 중복을 제거합니다.
         *
         * LinkedHashSet을 사용해 요청 순서는 유지합니다.
         */
        List<Long> distinctTagIds =
                new LinkedHashSet<>(tagIds)
                        .stream()
                        .toList();

        List<Tag> foundTags =
                tagRepository.findAllById(
                        distinctTagIds
                );

        /*
         * 요청한 ID 개수와 실제 조회된 태그 개수가 다르면
         * 존재하지 않는 태그 ID가 포함된 것입니다.
         */
        if (foundTags.size()
                != distinctTagIds.size()) {

            throw new BusinessException(
                    ErrorCode.TAG_NOT_FOUND
            );
        }

        boolean invalidTagTypeExists =
                foundTags.stream()
                        .anyMatch(tag ->
                                tag.getType()
                                        != expectedType
                        );

        if (invalidTagTypeExists) {
            throw new BusinessException(
                    ErrorCode.INVALID_TAG_TYPE
            );
        }

        /*
         * findAllById()의 결과 순서는 보장되지 않으므로,
         * 요청으로 전달된 tagIds 순서대로 다시 정렬합니다.
         */
        Map<Long, Tag> tagById =
                foundTags.stream()
                        .collect(
                                Collectors.toMap(
                                        Tag::getId,
                                        Function.identity()
                                )
                        );

        List<Tag> orderedTags =
                distinctTagIds.stream()
                        .map(tagById::get)
                        .toList();

        /*
         * 기존 태그를 JPQL 벌크 DELETE로 즉시 삭제합니다.
         *
         * 기존 태그 중 일부를 다시 선택했을 때
         * DELETE보다 INSERT가 먼저 실행되면서
         * (user_id, tag_id) UNIQUE 제약조건을 위반하는
         * 문제를 방지합니다.
         */
        userTagRepository
                .deleteAllByUserIdAndTagType(
                        user.getId(),
                        expectedType
                );

        /*
         * 빈 배열이면 기존 태그 삭제까지만 하고 종료합니다.
         */
        if (orderedTags.isEmpty()) {
            return;
        }

        List<UserTag> newUserTags =
                orderedTags.stream()
                        .map(tag ->
                                new UserTag(
                                        user,
                                        tag
                                )
                        )
                        .toList();

        userTagRepository
                .saveAllAndFlush(newUserTags);
    }
}