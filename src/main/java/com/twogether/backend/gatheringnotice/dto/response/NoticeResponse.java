package com.twogether.backend.gatheringnotice.dto.response;

import com.twogether.backend.gatheringnotice.domain.GatheringNotice;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "공지사항 상세 응답")
public record NoticeResponse(

        @Schema(description = "공지사항 ID", example = "1")
        Long noticeId,

        @Schema(description = "모임 ID", example = "1")
        Long gatheringId,

        @Schema(description = "공지 제목", example = "중요 공지사항입니다.")
        String title,

        @Schema(description = "공지 내용", example = "이번 주 모임 장소가 명진당 1층으로 변경되었습니다.")
        String content,

        @Schema(description = "상단 고정 여부", example = "true")
        boolean isPinned,

        @Schema(description = "생성 일시", example = "2026-07-17T13:00:00")
        LocalDateTime createdAt,

        @Schema(description = "수정 일시", example = "2026-07-17T13:00:00")
        LocalDateTime updatedAt

) {

    public static NoticeResponse from(GatheringNotice notice) {
        return new NoticeResponse(
                notice.getId(),
                notice.getGathering().getId(),
                notice.getTitle(),
                notice.getContent(),
                notice.isPinned(),
                notice.getCreatedAt(),
                notice.getUpdatedAt()
        );
    }
}
