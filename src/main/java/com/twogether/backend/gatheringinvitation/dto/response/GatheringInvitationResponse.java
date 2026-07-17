package com.twogether.backend.gatheringinvitation.dto.response;

import com.twogether.backend.gatheringinvitation.domain.GatheringInvitation;
import com.twogether.backend.gatheringinvitation.domain.InvitationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "모임 초대 응답")
public record GatheringInvitationResponse(

        @Schema(description = "초대 ID")
        Long invitationId,

        @Schema(description = "모임 ID")
        Long gatheringId,

        @Schema(description = "모임 제목")
        String gatheringTitle,

        @Schema(description = "초대자 정보")
        UserBriefResponse inviter,

        @Schema(description = "피초대자 정보")
        UserBriefResponse invitee,

        @Schema(description = "초대 메시지")
        String message,

        @Schema(description = "초대 상태")
        InvitationStatus status,

        @Schema(description = "거절/취소 사유")
        String reason,

        @Schema(description = "초대 시간")
        OffsetDateTime invitedAt,

        @Schema(description = "응답 시간")
        OffsetDateTime respondedAt

) {
    public static GatheringInvitationResponse of(
            GatheringInvitation invitation,
            String inviterDepartmentName,
            String inviterCampus,
            String inviteeDepartmentName,
            String inviteeCampus
    ) {
        return new GatheringInvitationResponse(
                invitation.getId(),
                invitation.getGathering().getId(),
                invitation.getGathering().getTitle(),
                UserBriefResponse.of(
                        invitation.getInviter().getId(),
                        invitation.getInviter().getNickname(),
                        inviterDepartmentName,
                        inviterCampus
                ),
                UserBriefResponse.of(
                        invitation.getInvitee().getId(),
                        invitation.getInvitee().getNickname(),
                        inviteeDepartmentName,
                        inviteeCampus
                ),
                invitation.getMessage(),
                invitation.getStatus(),
                invitation.getReason(),
                invitation.getInvitedAt(),
                invitation.getRespondedAt()
        );
    }
}
