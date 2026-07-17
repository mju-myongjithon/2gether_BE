package com.twogether.backend.notification.controller;

import com.twogether.backend.global.response.ApiResponse;
import com.twogether.backend.global.response.PageResponse;
import com.twogether.backend.notification.dto.response.NotificationResponse;
import com.twogether.backend.notification.dto.response.NotificationUnreadCountResponse;
import com.twogether.backend.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "알림 API",
        description = "인앱 알림함 조회, 안읽음 수, 읽음 처리 관련 API"
)
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Operation(
            summary = "내 알림 목록 조회",
            description = "내 알림을 최신순으로 조회합니다. page=0, size=20 기본."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<NotificationResponse> response =
                notificationService.getMyNotifications(jwt.getSubject(), page, size);

        return ResponseEntity.ok(
                ApiResponse.success("알림 목록 조회에 성공했습니다.", response)
        );
    }

    @Operation(
            summary = "안읽은 알림 수 조회",
            description = "벨 배지에 표시할 안읽은 알림 수를 반환합니다."
    )
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<NotificationUnreadCountResponse>> getUnreadCount(
            @AuthenticationPrincipal Jwt jwt
    ) {
        NotificationUnreadCountResponse response =
                notificationService.getUnreadCount(jwt.getSubject());

        return ResponseEntity.ok(
                ApiResponse.success("안읽은 알림 수 조회에 성공했습니다.", response)
        );
    }

    @Operation(
            summary = "알림 단건 읽음 처리",
            description = "특정 알림을 읽음 처리합니다. 본인 알림만 가능하며, 이미 읽은 경우도 정상 처리됩니다."
    )
    @PostMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> readNotification(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long notificationId
    ) {
        notificationService.markAsRead(jwt.getSubject(), notificationId);

        return ResponseEntity.ok(
                ApiResponse.success("알림을 읽음 처리했습니다.")
        );
    }

    @Operation(
            summary = "알림 전체 읽음 처리",
            description = "내 미읽음 알림을 모두 읽음 처리합니다."
    )
    @PostMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> readAllNotifications(
            @AuthenticationPrincipal Jwt jwt
    ) {
        notificationService.markAllAsRead(jwt.getSubject());

        return ResponseEntity.ok(
                ApiResponse.success("모든 알림을 읽음 처리했습니다.")
        );
    }
}
