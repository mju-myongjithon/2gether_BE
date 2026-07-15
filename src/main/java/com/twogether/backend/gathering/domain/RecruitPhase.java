package com.twogether.backend.gathering.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DB에 저장되는 값이 아니라, gathering.status와
 * recruit_start_at / recruit_end_at을 현재 시각과 비교해
 * API 응답 시점에 계산해서 내려주는 값입니다.
 */
@Schema(description = "모집 진행 단계 (계산값, DB 저장값 아님)")
public enum RecruitPhase {

    @Schema(description = "모집 예정 (아직 모집 시작 전)")
    UPCOMING,

    @Schema(description = "모집 중")
    OPEN,

    @Schema(description = "모집 마감/종료")
    CLOSED,

    @Schema(description = "상시 모집 (모집 마감일 없음)")
    ALWAYS_OPEN
}
