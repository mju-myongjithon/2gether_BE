package com.twogether.backend.user.dto;

public class OnboardingRequest {

    private String realName;
    private String nickname;
    private Integer age;
    private String studentNumber;
    private Long departmentId;
    private String preferredRegion;
    private String profileImageUrl;

    protected OnboardingRequest() {
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

    public Long getDepartmentId() {
        return departmentId;
    }

    public String getPreferredRegion() {
        return preferredRegion;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }
}