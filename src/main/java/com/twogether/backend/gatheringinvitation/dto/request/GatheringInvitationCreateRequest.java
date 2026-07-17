package com.twogether.backend.gatheringinvitation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "모임 초대 생성 요청")
public record GatheringInvitationCreateRequest(

        @Schema(description = "초대할 사용자 ID", example = "123")
        @NotNull(message = "초대할 사용자 ID는 필수입니다.")
        Long inviteeId,

        @Schema(description = "초대 메시지", example = "함께 모임에 참여해주세요!")
        @Size(max = 300, message = "초대 메시지는 300자 이하로 입력해주세요.")
        String message

) {
}
