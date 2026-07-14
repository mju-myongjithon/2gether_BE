package com.twogether.backend.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "간단한 자기소개 수정 요청")
public record IntroductionUpdateRequest(

        @Schema(
                description = "사용자의 간단한 자기소개입니다. 최대 200자까지 입력할 수 있습니다.",
                example = "백엔드 개발과 운동을 좋아합니다.",
                nullable = true
        )
        String introduction

) {
}