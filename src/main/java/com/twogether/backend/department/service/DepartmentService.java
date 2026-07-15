package com.twogether.backend.department.service;

import com.twogether.backend.department.domain.Department;
import com.twogether.backend.department.dto.response.DepartmentResponse;
import com.twogether.backend.department.repository.DepartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(
            DepartmentRepository departmentRepository
    ) {
        this.departmentRepository = departmentRepository;
    }

    public List<DepartmentResponse> getDepartments() {

        return departmentRepository
                .findAllByOrderByIdAsc()
                .stream()
                .map(this::toDepartmentResponse)
                .toList();
    }

    private DepartmentResponse toDepartmentResponse(
            Department department
    ) {
        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getCollege()
                        .getCampus()
                        .name()
        );
    }
}