package com.twogether.backend.contentrecommendation.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "추천 콘텐츠 유형")
public enum ContentType {
    BLOG,
    ARTICLE,
    EVENT,
    PLACE,
    ACTIVITY
}
