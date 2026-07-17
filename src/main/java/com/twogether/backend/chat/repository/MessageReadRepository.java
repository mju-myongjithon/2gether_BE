package com.twogether.backend.chat.repository;

import com.twogether.backend.chat.domain.MessageRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MessageReadRepository extends JpaRepository<MessageRead, Long> {

    Optional<MessageRead> findByMessageIdAndUserId(Long messageId, Long userId);

    @Query("SELECT COUNT(mr) FROM MessageRead mr WHERE mr.message.id = :messageId")
    long countByMessageId(@Param("messageId") Long messageId);

    @Query("SELECT mr.user.id FROM MessageRead mr WHERE mr.message.id = :messageId")
    List<Long> findUserIdsByMessageId(@Param("messageId") Long messageId);

    @Query("SELECT CASE WHEN COUNT(mr) > 0 THEN true ELSE false END FROM MessageRead mr WHERE mr.message.id = :messageId AND mr.user.id = :userId")
    boolean existsByMessageIdAndUserId(@Param("messageId") Long messageId, @Param("userId") Long userId);
}
