package com.twogether.backend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // JWT 방식의 REST API이므로 CSRF 비활성화
                .csrf(csrf -> csrf.disable())

                // 로그인 정보를 서버 세션에 저장하지 않음
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth
                        // Swagger는 로그인 없이 접근 허용
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/login-test.html"
                        ).permitAll()

                        // 서버 상태 확인 API 허용
                        .requestMatchers(
                                "/actuator/health",
                                "/api/health"
                        ).permitAll()

                        // 닉네임 중복 확인은 로그인 전에 사용할 수 있으므로 허용
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/users/nickname/check"
                        ).permitAll()

                        // 모임 목록/상세/멤버 목록 조회는 로그인 없이도 열람할 수 있도록 허용
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/gatherings",
                                "/api/gatherings/*",
                                "/api/gatherings/*/members"
      