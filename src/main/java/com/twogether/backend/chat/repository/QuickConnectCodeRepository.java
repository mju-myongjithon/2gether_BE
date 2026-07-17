package com.twogether.backend.chat.repository;

import com.twogether.backend.chat.domain.QuickCodeStatus;
import com.twogether.backend.chat.domain.QuickConnectCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuickConnectCodeRepository
        extends JpaRepository<QuickConnectCode, Long> {

    /*
     * 특정 상태의 코드 조회 (join 시 ACTIVE 코드 탐색).
     */
    Optional<QuickConnectCode> findByCodeAndStatus(
            String code,
            QuickCodeStatus status
    );

    /*
     * 코드 값으로 최신 1건 (실패 사유 판별용: USED/EXPIRED/없음).
     */
    Optional<QuickConnectCode> findFirstByCodeOrderByCreatedAtDesc(
            String code
    );

    /*
     * 활성 코드 유일성 보장을 위한 중복 확인(코드 생성 시).
     */
    boolean existsByCodeAndStatus(
            String code,
            QuickCodeStatus status
    );
}
