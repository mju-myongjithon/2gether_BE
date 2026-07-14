package com.twogether.backend.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "auth_user_id",
            nullable = false,
            unique = true
    )
    private String authUserId;

    @Column(name = "real_name")
    private String realName;

    @Column(
            name = "nickname",
            unique = true
    )
    private String nickname;

    @Column(name = "age")
    private Integer age;

    @Column(name = "student_number")
    private String studentNumber;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "preferred_region")
    private String preferredRegion;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(
            name = "email_verified",
            nullable = false
    )
    private boolean emailVerified;

    @Column(
            name = "profile_completed",
            nullable = false
    )
    private boolean profileCompleted;

    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    protected User() {
    }

    public User(String authUserId) {
        this.authUserId = authUserId;
        this.emailVerified = false;
        this.profileCompleted = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getAuthUserId() {
        return authUserId;
    }

    public String getRealName() {
        return realName;
    }

    public String getNickname() {
        return nickname;
    }

    public Integer getAge() {
        return age;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public String getPreferredRegion() {
        return preferredRegion;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public boolean isProfileCompleted() {
        return profileCompleted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void completeProfile(
            String realName,
            String nickname,
            Integer age,
            String studentNumber,
            Long departmentId,
            String preferredRegion
    ) {
        this.realName = realName;
        this.nickname = nickname;
        this.age = age;
        this.studentNumber = studentNumber;
        this.departmentId = departmentId;
        this.preferredRegion = preferredRegion;
        this.profileCompleted = true;
        this.updatedAt = LocalDateTime.now();
    }
    public Long getDepartmentId() {
        return departmentId;
    }
}