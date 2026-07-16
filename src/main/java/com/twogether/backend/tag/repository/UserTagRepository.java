package com.twogether.backend.tag.repository;

import com.twogether.backend.tag.domain.TagType;
import com.twogether.backend.tag.domain.UserTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserTagRepository
        extends JpaRepository<UserTag, Long> {

    List<UserTag> findAllByUser_IdOrderByTag_IdAsc(
            Long userId
    );

    void deleteAllByUser_IdAndTag_Type(
            Long userId,
            TagType type
    );

    void deleteAllByUser_Id(
            Long userId
    );
}