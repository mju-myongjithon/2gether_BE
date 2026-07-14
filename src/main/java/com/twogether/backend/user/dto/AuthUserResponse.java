package com.twogether.backend.user.dto;

import com.twogether.backend.user.domain.User;

public class AuthUserResponse {

    private final Long userId;
    private final boolean profileCompleted;

    public AuthUserResponse(Long userId, boolean profileCompleted) {
        this.userId = userId;
        this.profileCompleted = profileCompleted;
    }

    public static AuthUserResponse from(User user) {
        return new AuthUserResponse(
                user.getId(),
                user.isProfileCompleted()
        );
    }

    public Long getUserId() {
        return userId;
    }

    public boolean isProfileCompleted() {
        return profileCompleted;
    }
}