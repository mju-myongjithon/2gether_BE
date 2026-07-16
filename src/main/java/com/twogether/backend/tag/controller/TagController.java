package com.twogether.backend.tag.controller;

import com.twogether.backend.global.response.ApiResponse;
import com.twogether.backend.tag.dto.request.HobbyTagUpdateRequest;
import com.twogether.backend.tag.dto.request.SkillTagUpdateRequest;
import com.twogether.backend.tag.dto.response.HobbyTagResponse;
import com.twogether.backend.tag.dto.response.SkillTagResponse;
import com.twogether.backend.tag.dto.response.UserTagsResponse;
import com.twogether.backend.tag.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(
        name = "태그 API",
        description = "취미 태그 및 기술 태그 관련 API"
)
@RestController
@RequestMapping("/api")
public class TagController {

    private final TagService tagService;

    public TagController(
            TagService tagService
    ) {
        this.tagService = tagService;
    }

    @Operation(
            summary = "취미 태그 목록 조회",
            description = """
                    온보딩 및 프로필 설정에서 선택할 수 있는
                    전체 취미 태그 목록을 조회합니다.

                    로그인 전에도 화면 구성을 위해 조회할 수 있으며,
                    취미 태그 정보는 데이터베이스에서 조회하여 반환합니다.
                    """
    )
    @GetMapping("/hobby-tags")
    public ResponseEntity<ApiResponse<List<HobbyTagResponse>>> getHobbyTags() {

        List<HobbyTagResponse> hobbyTags =
                tagService.getHobbyTags();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "취미 태그 목록 조회에 성공했습니다.",
                        hobbyTags
                )
        );
    }

    @Operation(
            summary = "기술 태그 목록 조회",
            description = """
                    온보딩 및 프로필 설정에서 선택할 수 있는
                    전체 기술 태그 목록을 조회합니다.

                    로그인 전에도 화면 구성을 위해 조회할 수 있으며,
                    기술 태그 정보는 데이터베이스에서 조회하여 반환합니다.
                    """
    )
    @GetMapping("/skill-tags")
    public ResponseEntity<ApiResponse<List<SkillTagResponse>>> getSkillTags() {

        List<SkillTagResponse> skillTags =
                tagService.getSkillTags();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "기술 태그 목록 조회에 성공했습니다.",
                        skillTags
                )
        );
    }

    @Operation(
            summary = "내 취미 태그 수정",
            description = """
                    현재 로그인한 사용자가 선택한 취미 태그 목록을
                    요청으로 전달된 tagIds 목록으로 전체 교체합니다.

                    기존에 선택한 취미 태그는 모두 삭제되고,
                    요청으로 전달된 취미 태그가 새로 저장됩니다.

                    빈 배열을 전달하면 현재 사용자의
                    취미 태그 선택을 모두 해제합니다.
                    """
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/users/me/hobby-tags")
    public ResponseEntity<ApiResponse<Void>> updateMyHobbyTags(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody HobbyTagUpdateRequest request
    ) {
        tagService.updateMyHobbyTags(
                jwt.getSubject(),
                request.tagIds()
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "취미 태그 수정에 성공했습니다."
                )
        );
    }

    @Operation(
            summary = "내 기술 태그 수정",
            description = """
                    현재 로그인한 사용자가 선택한 기술 태그 목록을
                    요청으로 전달된 tagIds 목록으로 전체 교체합니다.

                    기존에 선택한 기술 태그는 모두 삭제되고,
                    요청으로 전달된 기술 태그가 새로 저장됩니다.

                    빈 배열을 전달하면 현재 사용자의
                    기술 태그 선택을 모두 해제합니다.
                    """
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/users/me/skill-tags")
    public ResponseEntity<ApiResponse<Void>> updateMySkillTags(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody SkillTagUpdateRequest request
    ) {
        tagService.updateMySkillTags(
                jwt.getSubject(),
                request.tagIds()
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "기술 태그 수정에 성공했습니다."
                )
        );
    }

    @Operation(
            summary = "특정 사용자 선택 태그 조회",
            description = """
                    특정 사용자가 실제로 선택한 취미 태그와
                    기술 태그를 조회합니다.

                    전체 선택 가능 태그 목록이 아니라,
                    해당 사용자가 저장한 태그만 반환합니다.

                    프로필 화면에서는 사용자의 태그를 표시할 수 있고,
                    태그 수정 화면에서는 기존 선택값을 표시할 수 있습니다.
                    """
    )
    @GetMapping("/users/{userId}/tags")
    public ResponseEntity<ApiResponse<UserTagsResponse>> getUserTags(
            @PathVariable Long userId
    ) {
        UserTagsResponse response =
                tagService.getUserTags(
                        userId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "사용자 태그 조회에 성공했습니다.",
                        response
                )
        );
    }
}