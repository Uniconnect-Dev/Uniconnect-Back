package com.uniConnect.notification.service;

import com.uniConnect.notification.entity.Notification;
import com.uniConnect.notification.enums.NotificationType;
import com.uniConnect.notification.repository.NotificationRepository;
import com.uniConnect.notification.service.PaymentNotificationService;
import com.uniConnect.payment.entity.Payment;
import com.uniConnect.payment.entity.RefundRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentNotificationServiceImpl implements PaymentNotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * 결제 성공 알림
     */
    @Override
    public void notifyPaymentSuccess(Payment payment) {
        String title = "결제 완료";
        String message = String.format(
                "금액 %,d원의 결제가 완료되었습니다. (결제일: %s)",
                payment.getAmount(),
                payment.getCreatedAt()
        );

        Notification notification = Notification.builder()
                .title(title)
                .message(message)
                .type(NotificationType.SystemAlert)
                .receiverRole("COMPANY_ADMIN")
                .relatedId(payment.getPaymentId())
                .relatedEntity("Payment")
                .isRead(false)
                .build();

        notificationRepository.save(notification);

        log.info("[결제 성공 알림] Payment ID: {}, Amount: {}, Company: {}",
                payment.getPaymentId(),
                payment.getAmount(),
                payment.getCompany().getCompanyId()
        );
    }

    /**
     * 결제 실패 알림
     */
    @Override
    public void notifyPaymentFailed(Payment payment) {
        String title = "결제 실패";
        String message = String.format(
                "금액 %,d원의 결제가 실패했습니다. 다시 시도해주세요.",
                payment.getAmount()
        );

        Notification notification = Notification.builder()
                .title(title)
                .message(message)
                .type(NotificationType.SystemAlert)
                .receiverRole("COMPANY_ADMIN")
                .relatedId(payment.getPaymentId())
                .relatedEntity("Payment")
                .isRead(false)
                .build();

        notificationRepository.save(notification);

        log.warn("[결제 실패 알림] Payment ID: {}, Amount: {}, Company: {}",
                payment.getPaymentId(),
                payment.getAmount(),
                payment.getCompany().getCompanyId()
        );
    }

    /**
     * 환불 요청 알림
     */
    @Override
    public void notifyRefundRequested(RefundRequest refund) {
        // 1) 회사(요청자)에게 알림
        String companyTitle = "환불 요청 접수";
        String companyMessage = String.format(
                "환불 요청이 접수되었습니다.\n" +
                        "요청 금액: %,d원\n" +
                        "상태: 검토 중",
                refund.getRefundAmount()
        );

        Notification companyNotification = Notification.builder()
                .title(companyTitle)
                .message(companyMessage)
                .type(NotificationType.SystemAlert)
                .receiverRole("COMPANY_ADMIN")
                .relatedId(refund.getRefundId())
                .relatedEntity("RefundRequest")
                .isRead(false)
                .build();

        notificationRepository.save(companyNotification);

        // 2) 관리자에게 알림
        String adminTitle = "새로운 환불 요청";
        String adminMessage = String.format(
                "새로운 환불 요청이 도착했습니다.\n" +
                        "요청자: %s (%s)\n" +
                        "요청 금액: %,d원\n" +
                        "사유: %s",
                refund.getRequesterName(),
                refund.getRequesterEmail(),
                refund.getRefundAmount(),
                refund.getReason()
        );

        Notification adminNotification = Notification.builder()
                .title(adminTitle)
                .message(adminMessage)
                .type(NotificationType.SystemAlert)
                .receiverRole("ADMIN")
                .relatedId(refund.getRefundId())
                .relatedEntity("RefundRequest")
                .isRead(false)
                .build();

        notificationRepository.save(adminNotification);

        log.info("[환불 요청 알림] Refund ID: {}, Amount: {}",
                refund.getRefundId(),
                refund.getRefundAmount()
//                refund.getCompany().getCompanyId()
        );
    }

    /**
     * 환불 완료 알림
     */
    @Override
    public void notifyRefundCompleted(RefundRequest refund) {
        String title = "환불 완료";
        String message = String.format(
                "환불이 완료되었습니다.\n" +
                        "환불 금액: %,d원\n" +
                        "예금주: %s\n" +
                        "은행: %s",
                refund.getRefundAmount(),
                refund.getHolderName(),
                refund.getBankName()
        );

        Notification notification = Notification.builder()
                .title(title)
                .message(message)
                .type(NotificationType.SystemAlert)
                .receiverRole("COMPANY_ADMIN")
                .relatedId(refund.getRefundId())
                .relatedEntity("RefundRequest")
                .isRead(false)
                .build();

        notificationRepository.save(notification);

        log.info("[환불 완료 알림] Refund ID: {}, Amount: {}",
                refund.getRefundId(),
                refund.getRefundAmount()
//                refund.getCompany().getCompanyId()
        );
    }

    /**
     * 일반 알림 저장
     */
    @Override
    public void notify(String title, String message, String type, Long relatedId, Long recipientId) {
        Notification notification = Notification.builder()
                .title(title)
                .message(message)
                .type(NotificationType.SystemAlert)
                .receiverRole("COMPANY_ADMIN")
                .relatedId(relatedId)
                .relatedEntity(type)
                .isRead(false)
                .build();

        notificationRepository.save(notification);

        log.info("[일반 알림] Title: {}, Type: {}, RelatedId: {}",
                title, type, relatedId
        );
    }
}