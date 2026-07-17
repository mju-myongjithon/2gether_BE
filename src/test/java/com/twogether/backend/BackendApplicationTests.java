package com.twogether.backend;

import com.twogether.backend.chat.service.ChatRoomService;
import com.twogether.backend.gathering.domain.Gathering;
import com.twogether.backend.gathering.domain.GatheringStatus;
import com.twogether.backend.gathering.repository.GatheringMemberRepository;
import com.twogether.backend.gathering.repository.GatheringRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
class BackendApplicationTests {

    @Autowired
    private GatheringRepository gatheringRepository;

    @Autowired
    private GatheringMemberRepository gatheringMemberRepository;

    @Autowired
    private ChatRoomService chatRoomService;

    @Test
    void contextLoads() {
    }

    @Test
    @Transactional
    @Commit
    void migrateConfirmedGatheringsChatRooms() {
        List<Gathering> confirmed = gatheringRepository.findAll().stream()
                .filter(g -> g.getStatus() == GatheringStatus.CONFIRMED)
                .toList();

        System.out.println("===== START MIGRATING EXISTING CONFIRMED GATHERINGS =====");
        System.out.println("Found confirmed gatherings count: " + confirmed.size());

        for (Gathering gathering : confirmed) {
            List<Long> memberUserIds = gatheringMemberRepository.findByGatheringIdWithUser(gathering.getId())
                    .stream()
                    .map(m -> m.getUser().getId())
                    .toList();

            Long chatRoomId = chatRoomService.createGroupRoom(
                    gathering.getId(),
                    gathering.getTitle(),
                    gathering.getHost().getId(),
                    memberUserIds
            );
            System.out.println("Migrated Gathering ID: " + gathering.getId() + " | Title: " + gathering.getTitle() + " -> ChatRoom ID: " + chatRoomId);
        }
        System.out.println("===== END MIGRATING EXISTING CONFIRMED GATHERINGS =====");
    }
}
