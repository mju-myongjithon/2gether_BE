package com.twogether.backend.gatheringapplication.repository;

import com.twogether.backend.gatheringapplication.domain.ApplicationStatus;
import com.twogether.backend.gatheringapplication.domain.GatheringApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface GatheringApplicationRepository
        extends JpaRepository<GatheringApplication, Long> {

    /**
     * 활성 신청(PENDING/ACCEPTED) 중복 여부 확인.
     * REJECTED 후 재신청을 허용하기 위해 상태를 함께 조건에 둔다.
     */
    boolean existsByGatheringIdAndUserIdAndStatusIn(
            Long gatheringId,
            Long userId,
            Collection<ApplicationStatus> statuses
    );
}
