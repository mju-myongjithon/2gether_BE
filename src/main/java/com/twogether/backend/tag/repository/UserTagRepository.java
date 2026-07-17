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

    @Query("""
            select ut
            from UserTag ut
            join fetch ut.tag
            where ut.user.id in :userIds
            order by ut.user.id asc, ut.tag.id asc
            """)
    List<UserTag> findAllByUserIdsWithTag(
            @Param("userIds") List<Long> userIds
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
