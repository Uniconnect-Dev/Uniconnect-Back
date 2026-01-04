package com.uniConnect.notification.repository;

import com.uniConnect.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    //모든 알림 조회
    List<Notification> findByReceiverRoleOrderByCreatedAtDesc(String receiverRole);
}