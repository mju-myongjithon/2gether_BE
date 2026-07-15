package com.twogether.backend.availability.repository;

import com.twogether.backend.availability.domain.Availability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AvailabilityRepository
        extends JpaRepository<Availability, Long> {

    List<Availability> findAllByUserAuthUserId(
            String authUserId
    );

    void deleteAllByUserAuthUserId(
            String authUserId
    );
}