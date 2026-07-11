package com.twogether.backend.availability.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "내 가용 일정 전체 저장 및 재설정 요청")
public record AvailabilityUpdateRequest(

        @Schema(
                description =  """
                        Touch UI에서 최종 선택된 가용 시간대 전체 목록입니다.
                        기존 일정은 전달된 목록으로 전체 교체됩니다.
                        특정 일정을 제외하면 해당 일정은 삭제되고,
                        빈 배열을 전달하면 모든 일정이 삭제됩니다.
                        """
        )
        List<AvailabilitySlotRequest> availabilities

) {
}