package com.uniConnect.notification.service;

import com.uniConnect.payment.entity.Payment;
import com.uniConnect.payment.entity.RefundRequest;

public interface PaymentNotificationService {

    /**
     * 결제 성공 알림
     */
    void notifyPaymentSuccess(Payment payment);

    /**
     * 결제 실패 알림
     */
    void notifyPaymentFailed(Payment payment);

    /**
     * 환불 요청 알림
     */
    void notifyRefundRequested(RefundRequest refund);

    /**
     * 환불 완료 알림
     */
    void notifyRefundCompleted(RefundRequest refund);

    /**
     * 일반 알림 저장
     */
    void notify(String title, String message, String type, Long relatedId, Long recipientId);
}