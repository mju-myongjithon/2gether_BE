package com.twogether.backend.chat.service;

import com.twogether.backend.chat.domain.ChatMemberRole;
import com.twogether.backend.chat.domain.ChatRoom;
import com.twogether.backend.chat.domain.ChatRoomMember;
import com.twogether.backend.chat.domain.Message;
import com.twogether.backend.chat.domain.MessageRead;
import com.twogether.backend.chat.dto.request.ChatReadRequest;
import com.twogether.backend.chat.dto.response.ChatReadBroadcastResponse;
import com.twogether.backend.chat.dto.response.ChatReadResponse;
import com.twogether.backend.chat.dto.response.ChatMessageResponse;
import com.twogether.backend.chat.dto.response.ChatNoticeResponse;
import com.twogether.backend.chat.dto.response.ChatRoomDetailResponse;
import com.twogether.backend.chat.dto.response.ChatRoomMemberResponse;
import com.twogether.backend.chat.dto.response.ChatRoomSummaryResponse;
import com.twogether.backend.chat.repository.ChatNoticeRepository;
import com.twogether.backend.chat.repository.ChatRoomMemberRepository;
import com.twogether.backend.chat.repository.ChatRoomRepository;
import com.twogether.backend.chat.repository.MessageReadRepository;
import com.twogether.backend.chat.repository.MessageRepository;
import com.twogether.backend.department.domain.Department;
import com.twogether.backend.department.repository.DepartmentRepository;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.global.response.PageResponse;
import com.twogether.backend.user.domain.User;
import com.twogether.backend.user.repository.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final ChatNoticeRepository chatNoticeRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final MessageRepository messageRepository;
    private final MessageReadRepository messageReadRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatRoomService(
            ChatRoomRepository chatRoomRepository,
            ChatNoticeRepository chatNoticeRepository,
            ChatRoomMemberRepository chatRoomMemberRepository,
            MessageRepository messageRepository,
            MessageReadRepository messageReadRepository,
            UserRepository userRepository,
            DepartmentRepository departmentRepository,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatNoticeRepository = chatNoticeRepository;
        this.chatRoomMemberRepository = chatRoomMemberRepository;
        this.messageRepository = messageRepository;
        this.messageReadRepository = messageReadRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.messagingTemplate = messagingTemplate;
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

        // 방장 정보 조회
        ChatRoomMember hostMember = chatRoomMemberRepository
                .findByChatRoomIdAndRoleAndLeftAtIsNull(room.getId(), ChatMemberRole.OWNER)
                .orElse(null);
        Long hostId = hostMember != null ? hostMember.getUser().getId() : null;
        String hostNickname = hostMember != null ? hostMember.getUser().getNickname() : null;

        return new ChatRoomSummaryResponse(
                room.getId(),
                room.getGatheringId(),
                room.getType(),
                room.getTitle(),
                memberCount,
                lastMessage,
                room.getLastMessageAt(),
                unreadCount,
                hostId,
                hostNickname
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

        ChatNoticeResponse activeNotice = chatNoticeRepository
                .findByChatRoomIdAndIsActiveTrue(roomId)
                .map(ChatNoticeResponse::from)
                .orElse(null);

        return new ChatRoomDetailResponse(
                room.getId(),
                room.getGatheringId(),
                room.getType(),
                room.getTitle(),
                room.getCreatedAt(),
                activeNotice,
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

    /**
     * 방을 읽음 처리한다. last_read_message_id 를 전진 방향으로만 갱신하고
     * 갱신 후의 안읽음 수를 계산해 반환한다.
     *
     * <p>요청 값이 방의 마지막 메시지 id 를 초과하면 마지막 메시지로 클램프하고,
     * 현재 값보다 과거면 무시(전진 전용)한다.</p>
     */
    @Transactional
    public ChatReadResponse markRead(
            String authUserId,
            Long roomId,
            ChatReadRequest request
    ) {
        User me = findUser(authUserId);

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        ChatRoomMember membership = chatRoomMemberRepository
                .findByChatRoomIdAndUserId(roomId, me.getId())
                .filter(ChatRoomMember::isParticipating)
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN));

        Long requested = request.lastReadMessageId();
        if (room.getLastMessageId() != null && requested > room.getLastMessageId()) {
            requested = room.getLastMessageId();
        }

        Long current = membership.getLastReadMessageId();
        Long newLastRead = (current == null || requested > current) ? requested : current;

        if (!newLastRead.equals(current)) {
            Message message = messageRepository.findById(newLastRead).orElse(null);
            if (message != null) {
                if (!messageReadRepository.existsByMessageIdAndUserId(newLastRead, me.getId())) {
                    messageReadRepository.save(new MessageRead(message, me));
                }
            }
        }

        membership.updateLastReadMessageId(newLastRead);

        // 읽음 위치가 전진했을 때만 방 구독자에게 실시간 반영(메시지별 안읽음 수 갱신용).
        if (!newLastRead.equals(current)) {
            messagingTemplate.convertAndSend(
                    BROADCAST_DESTINATION_PREFIX + roomId + "/read",
                    new ChatReadBroadcastResponse(
                            roomId,
                            me.getId(),
                            current == null ? 0L : current,
                            newLastRead
                    )
            );
        }

        int unreadCount = (int) messageRepository
                .countByChatRoomIdAndIdGreaterThan(roomId, newLastRead);

        return new ChatReadResponse(roomId, newLastRead, unreadCount);
    }

    private static final String BROADCAST_DESTINATION_PREFIX = "/sub/chat/rooms/";

    /**
     * 채팅방에서 나간다(소프트: left_at 세팅). 메시지 이력은 보존되고 목록/상세에서 제외된다.
     *
     * <p>퇴장 SYSTEM 메시지를 발행/브로드캐스트한다. 방장이 나가면 남은 참여자 중 가장 먼저
     * 입장한 사람에게 방장을 위임하고, 남은 참여자가 없으면 방을 종료(closed_at)한다.</p>
     */
    @Transactional
    public void leave(
            String authUserId,
            Long roomId
    ) {
        User me = findUser(authUserId);

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        ChatRoomMember membership = chatRoomMemberRepository
                .findByChatRoomIdAndUserId(roomId, me.getId())
                .filter(ChatRoomMember::isParticipating)
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN));

        boolean wasOwner = membership.getRole() == ChatMemberRole.OWNER;
        membership.leave();

        publishSystemMessage(
                room,
                me.getNickname() + "님이 나갔습니다.",
                Map.of("systemType", "MEMBER_LEFT", "userId", me.getId())
        );

        // leave() 로 인한 left_at 갱신은 아래 조회 시 auto-flush 되어 반영된다.
        List<ChatRoomMember> remaining = chatRoomMemberRepository
                .findAllByChatRoomIdAndLeftAtIsNull(roomId);

        if (remaining.isEmpty()) {
            room.close();
            return;
        }

        if (wasOwner) {
            ChatRoomMember successor = remaining.stream()
                    .min(Comparator.comparing(ChatRoomMember::getJoinedAt))
                    .orElseThrow();
            successor.promoteToOwner();
            publishSystemMessage(
                    room,
                    successor.getUser().getNickname() + "님이 방장이 되었습니다.",
                    Map.of("systemType", "OWNER_DELEGATED", "userId", successor.getUser().getId())
            );
        }
    }

    private void publishSystemMessage(
            ChatRoom room,
            String content,
            Map<String, Object> meta
    ) {
        Message systemMessage = messageRepository.save(
                Message.system(room, content, meta)
        );
        room.updateLastMessage(systemMessage.getId(), systemMessage.getCreatedAt());
        messagingTemplate.convertAndSend(
                BROADCAST_DESTINATION_PREFIX + room.getId(),
                ChatMessageResponse.from(systemMessage)
        );
    }

    private User findUser(
            String authUserId
    ) {
        return userRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
