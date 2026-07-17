package com.twogether.backend.chat.dto.response;

import com.twogether.backend.chat.domain.ChatMemberRole;
import com.twogether.backend.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채팅방 참여자 정보")
public record ChatRoomMemberResponse(

        @Schema(description = "사용자 ID", example = "1")
        Long userId,

        @Schema(description = "닉네임", example = "인준")
        String nickname,

        @Schema(description = "학과명", example = "컴퓨터공학과")
        String departmentName,

        @Schema(description = "캠퍼스", example = "NATURAL", allowableValues = {"HUMANITIES", "NATURAL"})
        String campus,

        @Schema(description = "역할", example = "OWNER", allowableValues = {"OWNER", "MEMBER"})
        ChatMemberRole role

) {

    public static ChatRoomMemberResponse of(
            User user,
            ChatMemberRole role,
            String departmentName,
            String campus
    ) {
        return new ChatRoomMemberResponse(
                user.getId(),
                user.getNickname(),
                departmentName,
                campus,
                role
        );
    }
}
