package com.twogether.backend.tag.service;

import com.twogether.backend.tag.domain.TagType;
import com.twogether.backend.tag.dto.response.HobbyTagResponse;
import com.twogether.backend.tag.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.twogether.backend.tag.dto.response.SkillTagResponse;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;

    public TagService(
            TagRepository tagRepository
    ) {
        this.tagRepository = tagRepository;
    }

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
}