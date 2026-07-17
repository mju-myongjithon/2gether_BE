package com.twogether.backend.availability.repository;

import com.twogether.backend.availability.domain.Availability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AvailabilityRepository
        extends JpaRepository<Availability, Long> {

    /*
     * 현재 로그인한 사용자의 Supabase 인증 ID를 기준으로
     * 본인 가용 일정을 조회합니다.
     */
    List<Availability> findAllByUserAuthUserId(
            String authUserId
    );

    /*
     * users 테이블의 PK를 기준으로
     * 특정 사용자의 가용 일정을 조회합니다.
     */
    List<Availability> findAllByUserId(
            Long userId
    );

    /*
     * 현재 로그인한 사용자의 기존 가용 일정을
     * 전체 삭제합니다.
     */
    void deleteAllByUserAuthUserId(
            String authUserId
    );
}