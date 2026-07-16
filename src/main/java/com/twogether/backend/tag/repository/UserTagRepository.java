package com.twogether.backend.tag.repository;

import com.twogether.backend.tag.domain.TagType;
import com.twogether.backend.tag.domain.UserTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserTagRepository
        extends JpaRepository<UserTag, Long> {

    List<UserTag> findAllByUser_IdOrderByTag_IdAsc(
            Long userId
    );

    @Modifying(flushAutomatically = true)
    @Query("""
            delete from UserTag ut
            where ut.user.id = :userId
              and ut.tag.type = :tagType
            """)
    int deleteAllByUserIdAndTagType(
            @Param("userId") Long userId,
            @Param("tagType") TagType tagType
    );

    void deleteAllByUser_Id(
            Long userId
    );
}