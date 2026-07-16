package com.twogether.backend.gatheringapplication.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "모임 신청 요청")
public record GatheringApplicationCreateRequest(

        @Schema(description = "신청 메시지", example = "컴퓨터공학과 학생인데 기획/디자인 전공 친구들과 협업해보고 싶습니다.")
        @Size(max = 300, message = "신청 메시지는 300자 이하로 입력해주세요.")
        String message

) {
}
