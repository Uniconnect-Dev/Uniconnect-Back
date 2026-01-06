package com.uniConnect.common.service;

import com.uniConnect.payment.entity.RefundRequest;

/**
 * 이메일 발송 서비스 인터페이스
 * 송장, 환불 등 다양한 알림 이메일을 발송합니다.
 */
public interface EmailService {
    /**
     * 회원가입 코드용 이메일 발송
     * @param recipientEmail 받는 사람
     * @param code
     */
    void sendVerificationCode(String recipientEmail, String code);

//    /**
//     * 세금계산서/영수증 발급 이메일 발송
//     * @param recipientEmail 받는 사람 이메일
//     * @param invoice 세금계산서 정보
//     * @param pdfUrl PDF 다운로드 URL
//     */
//    void sendInvoiceEmail(String recipientEmail, Invoice invoice, String pdfUrl);
//
    /**
     * 환불 요청 접수 이메일 발송
     * @param recipientEmail 받는 사람 이메일
     * @param refundRequest 환불 요청 정보
     */
    void sendRefundRequestConfirmationEmail(String recipientEmail, RefundRequest refundRequest);

    /**
     * 환불 거절 이메일 발송
     * @param recipientEmail 받는 사람 이메일
     * @param refundRequest 환불 요청 정보
     */
    void sendRefundRejectionEmail(String recipientEmail, RefundRequest refundRequest);

    /**
     * 환불 완료 이메일 발송
     * @param recipientEmail 받는 사람 이메일
     * @param refundRequest 환불 요청 정보
     */
    void sendRefundCompletionEmail(String recipientEmail, RefundRequest refundRequest);
}