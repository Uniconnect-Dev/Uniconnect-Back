package com.uniConnect.notification.entity;

import com.uniConnect.notification.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "receiver_role", nullable = false)
    private String receiverRole; // e.g. ADMIN, COMPANY, STUDENT_ORG

    @Column(name = "is_read", nullable = false)
    private Boolean isRead;

    @Column(name = "related_id")
    private Long relatedId; // 관련 캠페인, 리포트 등 ID

    @Column(name = "related_entity")
    private String relatedEntity;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
    }
}