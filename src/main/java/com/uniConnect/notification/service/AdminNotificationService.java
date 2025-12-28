package com.uniConnect.notification.service;

import com.uniConnect.notification.entity.Notification;
import com.uniConnect.notification.enums.NotificationType;
import com.uniConnect.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminNotificationService {

    private final NotificationRepository notificationRepository;

    /** 어드민에게 알림 저장 */
    public void notifyAdmin(String title, String message, NotificationType type, Long relatedId, String relatedEntity) {
        Notification notification = Notification.builder()
                .title(title)
                .message(message)
                .type(type)
                .receiverRole("ADMIN")
                .relatedId(relatedId)
                .relatedEntity(relatedEntity)
                .build();

        notificationRepository.save(notification);

        log.info("[ADMIN 알림] {} - {}", title, message);
    }

    /** 단축 버전 (리포트 제출 등 간단 알림) */
    public void notifyAdmin(String message) {
        notifyAdmin("새로운 알림", message, NotificationType.SystemAlert, null, null);
    }
}