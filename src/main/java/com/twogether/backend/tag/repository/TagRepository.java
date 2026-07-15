package com.twogether.backend.tag.repository;

import com.twogether.backend.tag.domain.Tag;
import com.twogether.backend.tag.domain.TagType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findAllByTypeOrderByIdAsc(
            TagType type
    );

    boolean existsByType(
            TagType type
    );
}