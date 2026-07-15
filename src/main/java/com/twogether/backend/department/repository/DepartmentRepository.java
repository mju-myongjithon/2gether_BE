package com.twogether.backend.department.repository;

import com.twogether.backend.department.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository
        extends JpaRepository<Department, Long> {

    List<Department> findAllByOrderByIdAsc();
}