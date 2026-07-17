package com.twogether.backend.recommendation.dto.request;

import com.twogether.backend.tag.domain.Tag;
import com.twogether.backend.tag.domain.TagType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI 추천에 전달할 태그 정보")
public record AiTagInfo(

        @Schema(
                description = "태그 고유 ID",
                example = "1"
        )
        Long tagId,

        @Schema(
                description = "태그 이름",
                example = "Spring Boot"
        )
        String name,

        @Schema(
                description = "태그 종류",
                example = "SKILL"
        )
        TagType type

) {

    public static AiTagInfo from(Tag tag) {
        return new AiTagInfo(
                tag.getId(),
                tag.getName(),
                tag.getType()
        );
    }
}