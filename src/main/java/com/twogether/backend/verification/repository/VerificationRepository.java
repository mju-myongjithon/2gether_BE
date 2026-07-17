package com.twogether.backend.verification.repository;

import com.twogether.backend.verification.domain.Verification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationRepository extends JpaRepository<Verification, Long> {
    boolean existsByGatheringId(Long gatheringId);
}
