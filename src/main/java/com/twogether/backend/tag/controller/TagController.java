package com.twogether.backend.tag.controller;

import com.twogether.backend.global.response.ApiResponse;
import com.twogether.backend.tag.dto.response.HobbyTagResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.twogether.backend.tag.dto.response.SkillTagResponse;

import java.util.List;

@Tag(
        name = "태그 API",
        description = "취미 태그 및 기술 태그 관련 API"
)
@RestController
@RequestMapping("/api")
public class TagController {

    @Operation(
            summary = "취미 태그 목록 조회",
            description = "온보딩 및 프로필 설정에서 선택 가능한 취미 태그 목록을 조회합니다."
    )
    @GetMapping("/hobby-tags")
    public ResponseEntity<ApiResponse<List<HobbyTagResponse>>> getHobbyTags() {

        List<HobbyTagResponse> hobbyTags = List.of(
                new HobbyTagResponse(1L, "스터디"),
                new HobbyTagResponse(2L, "운동"),
                new HobbyTagResponse(3L, "맛집 탐방"),
                new HobbyTagResponse(4L, "보드게임")
        );

        ApiResponse<List<HobbyTagResponse>> response =
                ApiResponse.success(
                        "취미 태그 목록 조회에 성공했습니다.",
                        hobbyTags
                );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "기술 태그 목록 조회",
            description = "온보딩 및 프로필 설정에서 선택 가능한 기술 태그 목록을 조회합니다."
    )
    @GetMapping("/skill-tags")
    public ResponseEntity<ApiResponse<List<SkillTagResponse>>> getSkillTags() {

        List<SkillTagResponse> skillTags = List.of(
                new SkillTagResponse(1L, "Spring Boot"),
                new SkillTagResponse(2L, "React"),
                new SkillTagResponse(3L, "Figma"),
                new SkillTagResponse(4L, "영상 편집")
        );

        ApiResponse<List<SkillTagResponse>> response =
                ApiResponse.success(
                        "기술 태그 목록 조회에 성공했습니다.",
                        skillTags
                );

        return ResponseEntity.ok(response);
    }
}