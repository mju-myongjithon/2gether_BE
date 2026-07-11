package com.twogether.backend.availability.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "내 가용 일정 전체 수정 요청")
public record AvailabilityUpdateRequest(

        @Schema(
                description = """
                        사용자가 최종 선택한 가용 시간대 목록입니다.
                        기존 일정은 이 목록으로 전체 교체되며,
                        빈 목록을 보내면 모든 가용 일정이 삭제됩니다.
                        """
        )
        List<AvailabilitySlotRequest> availabilities

) {
}