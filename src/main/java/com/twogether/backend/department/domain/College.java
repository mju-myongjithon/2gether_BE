package com.twogether.backend.department.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "colleges")
public class College {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "name",
            nullable = false,
            unique = true
    )
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "campus",
            nullable = false
    )
    private Campus campus;

    protected College() {
    }

    public College(
            String name,
            Campus campus
    ) {
        this.name = name;
        this.campus = campus;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Campus getCampus() {
        return campus;
    }
}