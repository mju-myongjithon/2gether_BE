package com.twogether.backend.user.repository;

import com.twogether.backend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByAuthUserId(String authUserId);

    boolean existsByNickname(String nickname);
}