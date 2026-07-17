package com.twogether.backend.chat.repository;

import com.twogether.backend.chat.domain.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository
        extends JpaRepository<Message, Long> {

    /*
     * 소켓 재전송 멱등 처리 (방 내 client_message_id 유일).
     */
    Optional<Message> findByChatRoomIdAndClientMessageId(
            Long chatRoomId,
            UUID clientMessageId
    );

    /*
     * 안읽음 수 = last_read_message_id 보다 큰 메시지 개수.
     */
    long countByChatRoomIdAndIdGreaterThan(
            Long chatRoomId,
            Long messageId
    );

    /*
     * 커서 페이징(첫 페이지): (chat_room_id, id) 키셋 역순.
     */
    List<Message> findByChatRoomIdOrderByIdDesc(
            Long chatRoomId,
            Pageable pageable
    );

    /*
     * 커서 페이징(다음 페이지): cursor(id)보다 과거 메시지를 역순으로.
     */
    List<Message> findByChatRoomIdAndIdLessThanOrderByIdDesc(
            Long chatRoomId,
            Long id,
            Pageable pageable
    );

    @Query("""
            select count(m.id)
            from Message m
            where m.chatRoom.id = :chatRoomId
              and m.createdAt >= :start
              and m.createdAt < :end
              and m.deletedAt is null
              and m.sender is not null
              and m.type <> com.twogether.backend.chat.domain.MessageType.SYSTEM
            """)
    long countActivityMessages(
            @Param("chatRoomId") Long chatRoomId,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );

    @Query("""
            select m.createdAt
            from Message m
            where m.chatRoom.id = :chatRoomId
              and m.createdAt >= :start
              and m.createdAt < :end
              and m.deletedAt is null
              and m.sender is not null
              and m.type <> com.twogether.backend.chat.domain.MessageType.SYSTEM
            """)
    List<OffsetDateTime> findActivityMessageCreatedAt(
            @Param("chatRoomId") Long chatRoomId,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );
}
