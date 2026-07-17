package com.twogether.backend.chat.repository;

import com.twogether.backend.chat.domain.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomMemberRepository
        extends JpaRepository<ChatRoomMember, Long> {

    /*
     * 특정 방의 특정 사용자 멤버십 조회 (참여/탈퇴 무관).
     */
    Optional<ChatRoomMember> findByChatRoomIdAndUserId(
            Long chatRoomId,
            Long userId
    );

    /*
     * 내가 현재 참여 중인(나가지 않은) 방 멤버십 목록.
     */
    List<ChatRoomMember> findAllByUserIdAndLeftAtIsNull(
            Long userId
    );

    /*
     * 특정 방의 현재 참여자 목록.
     */
    List<ChatRoomMember> findAllByChatRoomIdAndLeftAtIsNull(
            Long chatRoomId
    );

    /*
     * 방 참여 권한 검증용.
     */
    /*
     * 방의 현재 참여자 수(멤버 카운트).
     */
    long countByChatRoomIdAndLeftAtIsNull(
            Long chatRoomId
    );

    boolean existsByChatRoomIdAndUserIdAndLeftAtIsNull(
            Long chatRoomId,
            Long userId
    );
}
