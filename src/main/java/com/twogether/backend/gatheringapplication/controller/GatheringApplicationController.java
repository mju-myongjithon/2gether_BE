package com.twogether.backend.gatheringapplication.controller;

import com.twogether.backend.gathering.domain.GatheringStatus;
import com.twogether.backend.gatheringapplication.domain.ApplicationStatus;
import com.twogether.backend.gatheringapplication.dto.request.GatheringApplicationCreateRequest;
import com.twogether.backend.gatheringapplication.dto.request.GatheringApplicationRejectRequest;
import com.twogether.backend.gatheringapplication.dto.response.ApplicantResponse;
import com.twogether.backend.gatheringapplication.dto.response.GatheringApplicationAcceptResponse;
import com.twogether.backend.gatheringapplication.dto.response.GatheringApplicationCreateResponse;
import com.twogether.backend.gatheringapplication.dto.response.GatheringApplicationRejectResponse;
import com.twogether.backend.gatheringapplication.dto.response.GatheringApplicationResponse;
import com.twogether.backend.gatheringapplication.dto.response.GatheringBriefResponse;
import com.twogether.backend.gatheringapplication.dto.response.MyApplicationResponse;
import com.twogether.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

@Tag(
        name = "모임 신청 API",
        description = "모임 참여 신청, 신청 목록 조회, 신청 수락/거절 관련 API"
)
@SecurityRequirement(name = "Bearer Authentication")
@RestController
public class GatheringApplicationController {

    @Operation(
            summary = "모임 신청",
            description = """
                    유저가 모집 중인 모임에 참여를 신청합니다.

                    같은 모임에는 한 번만 신청할 수 있습니다.

                    현재 Swagger 명세 단계에서는 실제 DB에 저장하지 않고
                    더미 응답을 반환합니다.
                    """
    )
    @PostMapping("/api/gatherings/{gatheringId}/applications")
    public ResponseEntity<ApiResponse<GatheringApplicationCreateResponse>> applyToGathering(
            @PathVariable Long gatheringId,
            @RequestBody GatheringApplicationCreateRequest request
    ) {
        GatheringApplicationCreateResponse response = new GatheringApplicationCreateResponse(
                1L,
                gatheringId,
                ApplicationStatus.PENDING,
                OffsetDateTime.parse("2026-07-09T19:30:00+09:00")
        );

        return ResponseEntity.ok(
                ApiResponse.success("모임 신청이 완료되었습니다.", response)
        );
    }

    @Operation(
            summary = "특정 모임의 신청자 목록 조회",
            description = """
                    방장만 조회할 수 있습니다.

                    현재 Swagger 명세 단계에서는 더미 신청자 목록을 반환합니다.
                    """
    )
    @GetMapping("/api/gatherings/{gatheringId}/applications")
    public ResponseEntity<ApiResponse<List<GatheringApplicationResponse>>> getGatheringApplications(
            @PathVariable Long gatheringId
    ) {
        ApplicantResponse applicant = new ApplicantResponse(
                2L,
                "기획러",
                "경영학과",
                "인문캠",
                List.of("맛집 탐방"),
                List.of("기획", "발표")
        );

        GatheringApplicationResponse response = new GatheringApplicationResponse(
                1L,
                ApplicationStatus.PENDING,
                "협업 경험을 쌓고 싶습니다.",
                OffsetDateTime.parse("2026-07-09T19:30:00+09:00"),
                applicant
        );

        return ResponseEntity.ok(
                ApiResponse.success("신청자 목록 조회에 성공했습니다.", List.of(response))
        );
    }

    @Operation(
            summary = "내 신청 목록 조회",
            description = """
                    현재 로그인한 사용자가 신청한 모임 목록을 조회합니다.

                    현재 Swagger 명세 단계에서는 더미 신청 목록을 반환합니다.
                    """
    )
    @GetMapping("/api/users/me/applications")
    public ResponseEntity<ApiResponse<List<MyApplicationResponse>>> getMyApplications() {
        GatheringBriefResponse gathering = new GatheringBriefResponse(
                1L,
                "인문X자연 해커톤 팀 모집",
                "해커톤",
                GatheringStatus.RECRUITING
        );

        MyApplicationResponse response = new MyApplicationResponse(
                1L,
                ApplicationStatus.PENDING,
                OffsetDateTime.parse("2026-07-09T19:30:00+09:00"),
                gathering
        );

        return ResponseEntity.ok(
                ApiResponse.success("내 신청 목록 조회에 성공했습니다.", List.of(response))
        );
    }

    @Operation(
            summary = "신청 수락",
            description = """
                    방장만 신청을 수락할 수 있습니다.

                    수락 시 gathering_application.status = ACCEPTED가 되고
                    gathering_member가 생성됩니다.

                    현재 Swagger 명세 단계에서는 실제 DB에 반영하지 않고
                    더미 응답을 반환합니다.
                    """
    )
    @PostMapping("/api/gathering-applications/{applicationId}/accept")
    public ResponseEntity<ApiResponse<GatheringApplicationAcceptResponse>> acceptApplication(
            @PathVariable Long applicationId
    ) {
        GatheringApplicationAcceptResponse response = new GatheringApplicationAcceptResponse(
                applicationId,
                ApplicationStatus.ACCEPTED,
                5L,
                OffsetDateTime.parse("2026-07-09T19:50:00+09:00")
        );

        return ResponseEntity.ok(
                ApiResponse.success("신청을 수락했습니다.", response)
        );
    }

    @Operation(
            summary = "신청 거절",
            description = """
                    방장만 신청을 거절할 수 있습니다.

                    거절되어도 신청 기록은 남습니다.

                    현재 Swagger 명세 단계에서는 실제 DB에 반영하지 않고
                    더미 응답을 반환합니다.
                    """
    )
    @PostMapping("/api/gathering-applications/{applicationId}/reject")
    public ResponseEntity<ApiResponse<GatheringApplicationRejectResponse>> rejectApplication(
            @PathVariable Long applicationId,
            @RequestBody GatheringApplicationRejectRequest request
    ) {
        GatheringApplicationRejectResponse response = new GatheringApplicationRejectResponse(
                applicationId,
                ApplicationStatus.REJECTED,
                request.rejectReason(),
                OffsetDateTime.parse("2026-07-09T19:55:00+09:00")
        );

        return ResponseEntity.ok(
                ApiResponse.success("신청을 거절했습니다.", response)
        );
    }
}
