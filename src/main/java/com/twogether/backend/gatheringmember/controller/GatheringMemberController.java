package com.twogether.backend.gatheringmember.controller;

import com.twogether.backend.gatheringmember.domain.GatheringMemberRole;
import com.twogether.backend.gatheringmember.dto.response.GatheringMemberListResponse;
import com.twogether.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

@Tag(
        name = "모임 멤버 API",
        description = "모임 멤버 목록 조회 및 탈퇴 관련 API"
)
@RestController
@RequestMapping("/api/gatherings/{gatheringId}/members")
public class GatheringMemberController {

    @Operation(
            summary = "모임 멤버 목록 조회",
            description = """
                    모임에 참여 중인 멤버 목록을 조회합니다.

                    현재 Swagger 명세 단계에서는 더미 멤버 목록을 반환합니다.
                    """
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<GatheringMemberListResponse>>> getGatheringMembers(
            @PathVariable Long gatheringId
    ) {
        List<GatheringMemberListResponse> members = List.of(
                new GatheringMemberListResponse(
                        1L, "인준", GatheringMemberRole.HOST, "컴퓨터공학과", "자연캠",
                        OffsetDateTime.parse("2026-07-09T19:00:00+09:00")
                ),
                new GatheringMemberListResponse(
                        2L, "기획러", GatheringMemberRole.MEMBER, "경영학과", "인문캠",
                        OffsetDateTime.parse("2026-07-09T19:50:00+09:00")
                )
        );

        return ResponseEntity.ok(
                ApiResponse.success("모임 멤버 목록 조회에 성공했습니다.", members)
        );
    }

    @Operation(
            summary = "모임 나가기",
            description = """
                    일반 멤버만 나갈 수 있습니다.

                    방장은 바로 나갈 수 없고, 모임 취소 또는 방장 위임 정책이 필요합니다.

                    현재 Swagger 명세 단계에서는 실제 DB에 반영하지 않고
                    성공 응답만 반환합니다.
                    """
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> leaveGathering(
            @PathVariable Long gatheringId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("모임에서 나갔습니다.")
        );
    }
}
