package com.twogether.backend.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "프로필 이미지 수정 요청")
public record ProfileImageUpdateRequest(

        @Schema(
                description = """
                        프론트에서 Supabase Storage에 이미지를 업로드한 뒤
                        전달받은 프로필 이미지 URL입니다.

                        null을 전달하면 기존 프로필 이미지를 제거하고
                        기본 프로필 이미지로 되돌립니다.

                        프로필 이미지는 선택 정보이며
                        온보딩 완료 여부에는 영향을 주지 않습니다.
                        """,
                example = "https://example.com/profile-images/user-1.jpg",
                nullable = true
        )
        String profileImageUrl

) {
}