package com.twogether.backend.gatheringmember.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "모임 멤버 역할")
public enum GatheringMemberRole {

    @Schema(description = "방장")
    HOST,

    @Schema(description = "일반 멤버")
    MEMBER
}
