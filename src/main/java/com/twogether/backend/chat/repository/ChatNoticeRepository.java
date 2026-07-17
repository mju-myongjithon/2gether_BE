package com.twogether.backend.chat.repository;

import com.twogether.backend.chat.domain.ChatNotice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatNoticeRepository
        extends JpaRepository<ChatNotice, Long> {

    /*
     * 방의 현재 활성 공지 1건.
     */
    Optional<ChatNotice> findByChatRoomIdAndIsActiveTrue(
            Long chatRoomId
    );
}
