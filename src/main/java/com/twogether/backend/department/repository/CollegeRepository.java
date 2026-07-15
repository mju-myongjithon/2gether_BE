package com.twogether.backend.department.repository;

import com.twogether.backend.department.domain.College;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CollegeRepository
        extends JpaRepository<College, Long> {

    List<College> findAllByOrderByIdAsc();
}