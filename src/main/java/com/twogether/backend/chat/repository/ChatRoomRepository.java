package com.twogether.backend.chat.repository;

import com.twogether.backend.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomRepository
        extends JpaRepository<ChatRoom, Long> {

    /*
     * 모임 자동 생성 방 조회 (gathering_id 1:1 소프트 참조).
     */
    Optional<ChatRoom> findByGatheringId(
            Long gatheringId
    );

    /*
     * 모임 채팅방 중복 생성 방지용 존재 확인.
     */
    boolean existsByGatheringId(
            Long gatheringId
    );
}
