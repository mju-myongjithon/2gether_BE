package com.twogether.backend.chat.service;

import com.twogether.backend.chat.domain.ChatRoom;
import com.twogether.backend.chat.domain.ChatRoomMember;
import com.twogether.backend.chat.domain.Message;
import com.twogether.backend.chat.dto.response.ChatRoomDetailResponse;
import com.twogether.backend.chat.dto.response.ChatRoomMemberResponse;
import com.twogether.backend.chat.dto.response.ChatRoomSummaryResponse;
import com.twogether.backend.chat.repository.ChatRoomMemberRepository;
import com.twogether.backend.chat.repository.ChatRoomRepository;
import com.twogether.backend.chat.repository.MessageRepository;
import com.twogether.backend.department.domain.Department;
import com.twogether.backend.department.repository.DepartmentRepository;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.global.response.PageResponse;
import com.twogether.backend.user.domain.User;
import com.twogether.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 채팅방 생성/조회 서비스.
 *
 * - 모임 확정 시 그룹 채팅방 자동 생성(gathering confirm 에서 호출).
 * - 내 채팅방 목록(최근순 + 마지막 메시지 미리보기 + 안읽음 수) / 방 상세(참여자) 조회.
 *
 * chat 도메인은 users / gathering / department 스키마를 수정하지 않고 참조만 한다.
 */
@Service
@Transactional(readOnly = true)
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    public ChatRoomService(
            ChatRoomRepository chatRoomRepository,
            ChatRoomMemberRepository chatRoomMemberRepository,
            MessageRepository messageRepository,
            UserRepository userRepository,
            DepartmentRepository departmentRepository
    ) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatRoomMemberRepository = chatRoomMemberRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
    }

    // ==================== 생성 (C2) ====================

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

    // ==================== 조회 (C3) ====================

    /**
     * 내가 참여 중인 채팅방 목록을 마지막 메시지 최근순으로 조회한다.
     * 마지막 메시지 미리보기와 방별 안읽음 수를 함께 실어 반환한다.
     */
    public PageResponse<ChatRoomSummaryResponse> getMyChatRooms(
            String authUserId,
            int page,
            int size
    ) {
        User me = findUser(authUserId);

        List<ChatRoomMember> memberships =
                chatRoomMemberRepository.findAllByUserIdAndLeftAtIsNull(me.getId());

        // 마지막 메시지 최근순(없으면 뒤로), 동률이면 방 생성 최근순
        memberships.sort(
                Comparator
                        .comparing(
                                (ChatRoomMember m) -> m.getChatRoom().getLastMessageAt(),
                                Comparator.nullsLast(Comparator.reverseOrder())
                        )
                        .thenComparing(
                                m -> m.getChatRoom().getCreatedAt(),
                                Comparator.reverseOrder()
                        )
        );

        int total = memberships.size();
        int from = Math.min(page * size, total);
        int to = Math.min(from + size, total);

        List<ChatRoomSummaryResponse> content = memberships.subList(from, to).stream()
                .map(this::toSummary)
                .toList();

        return PageResponse.of(content, page, size, total);
    }

    private ChatRoomSummaryResponse toSummary(
            ChatRoomMember membership
    ) {
        ChatRoom room = membership.getChatRoom();

        int memberCount = (int) chatRoomMemberRepository
                .countByChatRoomIdAndLeftAtIsNull(room.getId());

        String lastMessage = room.getLastMessageId() == null
                ? null
                : messageRepository.findById(room.getLastMessageId())
                        .map(this::messagePreview)
                        .orElse(null);

        long lastReadId = membership.getLastReadMessageId() == null
                ? 0L
                : membership.getLastReadMessageId();
        int unreadCount = (int) messageRepository
                .countByChatRoomIdAndIdGreaterThan(room.getId(), lastReadId);

        return new ChatRoomSummaryResponse(
                room.getId(),
                room.getGatheringId(),
                room.getType(),
                room.getTitle(),
                memberCount,
                lastMessage,
                room.getLastMessageAt(),
                unreadCount
        );
    }

    private String messagePreview(
            Message message
    ) {
        if (message.isDeleted()) {
            return "삭제된 메시지입니다.";
        }
        return switch (message.getType()) {
            case IMAGE -> "(이미지)";
            case TEXT, SYSTEM, CARD -> message.getContent();
        };
    }

    /**
     * 채팅방 상세(기본 정보 + 참여자 목록). 참여 중인 사용자만 조회 가능.
     */
    public ChatRoomDetailResponse getChatRoomDetail(
            String authUserId,
            Long roomId
    ) {
        User me = findUser(authUserId);

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        boolean participating = chatRoomMemberRepository
                .existsByChatRoomIdAndUserIdAndLeftAtIsNull(roomId, me.getId());
        if (!participating) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        List<ChatRoomMember> members =
                chatRoomMemberRepository.findAllByChatRoomIdAndLeftAtIsNull(roomId);

        Map<Long, Department> departmentMap = loadDepartments(members);

        List<ChatRoomMemberResponse> memberResponses = members.stream()
                .map(member -> toMemberResponse(member, departmentMap))
                .toList();

        return new ChatRoomDetailResponse(
                room.getId(),
                room.getGatheringId(),
                room.getType(),
                room.getTitle(),
                room.getCreatedAt(),
                memberResponses
        );
    }

    private Map<Long, Department> loadDepartments(
            List<ChatRoomMember> members
    ) {
        Set<Long> departmentIds = members.stream()
                .map(member -> member.getUser().getDepartmentId())
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        if (departmentIds.isEmpty()) {
            return Map.of();
        }

        return departmentRepository.findAllById(departmentIds).stream()
                .collect(Collectors.toMap(Department::getId, Function.identity()));
    }

    private ChatRoomMemberResponse toMemberResponse(
            ChatRoomMember member,
            Map<Long, Department> departmentMap
    ) {
        User user = member.getUser();
        Department department = user.getDepartmentId() == null
                ? null
                : departmentMap.get(user.getDepartmentId());

        String departmentName = department == null ? null : department.getName();
        String campus = department == null
                ? null
                : department.getCollege().getCampus().name();

        return ChatRoomMemberResponse.of(
                user,
                member.getRole(),
                departmentName,
                campus
        );
    }

    private User findUser(
            String authUserId
    ) {
        return userRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
