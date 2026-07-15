package com.twogether.backend.department.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "name",
            nullable = false
    )
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "college_id",
            nullable = false
    )
    private College college;

    protected Department() {
    }

    public Department(
            String name,
            College college
    ) {
        this.name = name;
        this.college = college;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public College getCollege() {
        return college;
    }
}