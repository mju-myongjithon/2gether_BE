package com.twogether.backend.chat.recommendation;

import com.twogether.backend.gathering.domain.GatheringCategory;

import java.util.List;

public record TopicRecommendationContext(
        Long chatRoomId,
        Long gatheringId,
        String gatheringTitle,
        String gatheringContent,
        GatheringCategory category,
        List<String> gatheringTags,
        List<String> participantInterestTags
) {
}
