package com.twogether.backend.chat.service;

import com.twogether.backend.chat.domain.ChatRoom;
import com.twogether.backend.chat.domain.ChatRoomMember;
import com.twogether.backend.chat.domain.Message;
import com.twogether.backend.chat.repository.ChatRoomMemberRepository;
import com.twogether.backend.chat.repository.ChatRoomRepository;
import com.twogether.backend.chat.repository.MessageRepository;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.user.domain.User;
import com.twogether.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 채팅방 생성/관리 서비스.
 *
 * 모임 확정(gathering confirm) 시 그룹 채팅방을 자동 생성하는 진입점을 제공한다.
 * chat 도메인은 gathering 스키마/코드를 수정하지 않고, gathering 측이 확정 트랜잭션 안에서
 * {@link #createGroupRoom} 를 호출하도록 "서비스 메서드"만 노출한다.
 */
@Service
@Transactional(readOnly = true)
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public ChatRoomService(
            ChatRoomRepository chatRoomRepository,
            ChatRoomMemberRepository chatRoomMemberRepository,
            MessageRepository messageRepository,
            UserRepository userRepository
    ) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatRoomMemberRepository = chatRoomMemberRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    /**
     * 모임 확정 시 그룹 채팅방을 생성하고 확정 멤버 전원을 참여자로 등록한다.
     *
     * <p>gathering confirm 트랜잭션 안에서 호출되며, 방·멤버·시스템 메시지가 원자적으로 생성된다.
     * {@code chat_room.gathering_id} 는 유니크이므로, 이미 방이 있으면(재호출/재시도) 기존 방 ID를
     * 그대로 반환해 멱등하게 동작한다.</p>
     *
     * @param gatheringId   모임 ID (chat_room.gathering_id, 소프트 참조)
     * @param title         방 제목 (모임 제목 스냅샷)
     * @param hostUserId    방장(users.id) — role=OWNER 로 등록
     * @param memberUserIds 확정 멤버(users.id) 목록 (hostUserId 포함/미포함 무관)
     * @return 생성(또는 기존) 채팅방 ID
     */
    @Transactional
    public Long createGroupRoom(
            Long gatheringId,
            String title,
            Long hostUserId,
            List<Long> memberUserIds
    ) {
        Optional<ChatRoom> existing = chatRoomRepository.findByGatheringId(gatheringId);
        if (existing.isPresent()) {
            return existing.get().getId();
        }

        ChatRoom room = chatRoomRepository.save(
                ChatRoom.group(gatheringId, title)
        );

        registerMembers(room, hostUserId, memberUserIds);
        publishRoomCreatedSystemMessage(room, gatheringId);

        return room.getId();
    }

    private void registerMembers(
            ChatRoom room,
            Long hostUserId,
            List<Long> memberUserIds
    ) {
        // 방장 우선 + 중복 제거(입력 순서 보존)
        LinkedHashSet<Long> userIds = new LinkedHashSet<>();
        userIds.add(hostUserId);
        if (memberUserIds != null) {
            userIds.addAll(memberUserIds);
        }

        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<ChatRoomMember> members = new ArrayList<>();
        for (Long userId : userIds) {
            User user = userMap.get(userId);
            if (user == null) {
                throw new BusinessException(ErrorCode.USER_NOT_FOUND);
            }
            members.add(
                    userId.equals(hostUserId)
                            ? ChatRoomMember.owner(room, user)
                            : ChatRoomMember.member(room, user)
            );
        }

        chatRoomMemberRepository.saveAll(members);
    }

    private void publishRoomCreatedSystemMessage(
            ChatRoom room,
            Long gatheringId
    ) {
        Message systemMessage = messageRepository.save(
                Message.system(
                        room,
                        "모임이 확정되어 채팅방이 열렸습니다.",
                        Map.of(
                                "systemType", "ROOM_CREATED",
                                "gatheringId", gatheringId
                        )
                )
        );

        room.updateLastMessage(
                systemMessage.getId(),
                systemMessage.getCreatedAt()
        );
    }
}
