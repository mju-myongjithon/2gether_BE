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

import java.util.List;
import java.util.Set;

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
                .findAllByTypeOrderByIdAsc(
                        TagType.HOBBY
                )
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
     * 선택 가능한 전체 기술 태그를 조회합니다.
     */
    public List<SkillTagResponse> getSkillTags() {

        return tagRepository
                .findAllByTypeOrderByIdAsc(
                        TagType.SKILL
                )
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
     * 현재 로그인한 사용자의 취미 태그를 전체 교체합니다.
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
     * 현재 로그인한 사용자의 기술 태그를 전체 교체합니다.
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
     * 특정 사용자가 선택한 취미 및 기술 태그를 조회합니다.
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
                                tag.getType()
                                        == TagType.HOBBY
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
                                tag.getType()
                                        == TagType.SKILL
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
     * 요청받은 태그 목록으로 사용자의 기존 태그를 전체 교체합니다.
     */
    private void replaceUserTags(
            String authUserId,
            List<Long> tagIds,
            TagType expectedType
    ) {
        User user = userRepository
                .findByAuthUserId(authUserId)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );

        if (tagIds == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_TAG_REQUEST
            );
        }

        /*
         * 중복 태그 ID를 제거합니다.
         *
         * 예: [1, 1, 2] → [1, 2]
         */
        Set<Long> uniqueTagIds =
                Set.copyOf(tagIds);

        List<Tag> tags =
                tagRepository.findAllById(
                        uniqueTagIds
                );

        /*
         * 요청한 ID 중 존재하지 않는 태그가 있는지 확인합니다.
         */
        if (tags.size() != uniqueTagIds.size()) {
            throw new BusinessException(
                    ErrorCode.TAG_NOT_FOUND
            );
        }

        /*
         * 취미 API에는 취미 태그만,
         * 기술 API에는 기술 태그만 요청할 수 있습니다.
         */
        boolean invalidTypeExists =
                tags.stream()
                        .anyMatch(tag ->
                                tag.getType()
                                        != expectedType
                        );

        if (invalidTypeExists) {
            throw new BusinessException(
                    ErrorCode.INVALID_TAG_TYPE
            );
        }

        /*
         * 기존에 선택한 같은 유형의 태그를 모두 삭제합니다.
         */
        userTagRepository
                .deleteAllByUser_IdAndTag_Type(
                        user.getId(),
                        expectedType
                );

        /*
         * 빈 배열이라면 기존 태그 삭제까지만 수행합니다.
         */
        if (tags.isEmpty()) {
            return;
        }

        List<UserTag> newUserTags =
                tags.stream()
                        .map(tag ->
                                new UserTag(
                                        user,
                                        tag
                                )
                        )
                        .toList();

        userTagRepository.saveAll(
                newUserTags
        );
    }
}