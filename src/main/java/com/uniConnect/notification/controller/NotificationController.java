package com.uniConnect.notification.controller;

import com.uniConnect.global.response.ApiResponse;
import com.uniConnect.notification.entity.Notification;
import com.uniConnect.notification.repository.NotificationRepository;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @Operation(summary = "어드민 알림 목록 조회")
    @GetMapping("/admin")
    public ApiResponse<List<Notification>> getAdminNotifications() {
        return ApiResponse.success(notificationRepository.findByReceiverRoleOrderByCreatedAtDesc("ADMIN"));
    }

    @Operation(summary = "알림 읽음 처리")
    @PostMapping("/{id}/read")
    public ApiResponse<String> markAsRead(@PathVariable Long id) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        n.setIsRead(true);
        notificationRepository.save(n);
        return ApiResponse.success("알림이 읽음 처리되었습니다.");
    }
}