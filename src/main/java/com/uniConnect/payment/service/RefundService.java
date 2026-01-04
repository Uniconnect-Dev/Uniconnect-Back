package com.uniConnect.payment.service;

import com.uniConnect.common.service.impl.AesEncryptionServiceImpl;
import com.uniConnect.global.exception.CustomException;
import com.uniConnect.global.exception.ErrorCode;
import com.uniConnect.notification.service.PaymentNotificationServiceImpl;
import com.uniConnect.payment.dto.*;
import com.uniConnect.payment.entity.DummyPgGatewayAdapter;
import com.uniConnect.payment.entity.Payment;
import com.uniConnect.payment.entity.RefundRequest;
import com.uniConnect.payment.enums.PaymentStatus;
import com.uniConnect.payment.enums.RefundStatus;
import com.uniConnect.payment.repository.*;
import com.uniConnect.sampling.repository.SamplingRequestRepository;
import lombok.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefundService {
    private final RefundRequestRepository refundRequestRepository;
    private final PaymentRepository paymentRepository;
    private final DummyPgGatewayAdapter pgGatewayAdapter;
    private final PaymentNotificationServiceImpl notificationService;
    private final EmailService emailService;
    private final AesEncryptionServiceImpl aesEncryptionServiceImpl;

    /**
     * 5. 환불 요청 생성
     */
    public PaymentDto.RefundResponse requestRefund(Long companyId, PaymentDto.RefundRequest request) {
        // 1) 결제 정보 조회
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        // 2) 권한 확인
        if (!payment.getCompany().getCompanyId().equals(companyId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // 3) 결제 상태 확인 (SUCCESS만 환불 가능)
        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 4) 환불 요청 엔티티 생성
        RefundRequest refund = RefundRequest.builder()
                .payment(payment)
                .reason(request.getReason())
                .refundAmount(request.getRefundAmount())
                .bankName(request.getBankName())
                .accountNoEnc(
                        aesEncryptionServiceImpl.encrypt(request.getAccountNo())
                )
                .holderName(request.getHolderName())
                .requesterName(request.getRequesterName())
                .requesterEmail(request.getRequesterEmail())
                .requesterPhone(request.getRequesterPhone())
                .requestedAt(LocalDateTime.now())
                .build();

        refundRequestRepository.save(refund);

        // 5) 알림 발송
        notificationService.notifyRefundRequested(refund);

        return convertToRefundResponse(refund);
    }

    /**
     * 환불 처리 (어드민)
     */
    public PaymentDto.RefundResponse processRefund(Long refundId, boolean approve, String rejectionReason) {
        RefundRequest refund = refundRequestRepository.findById(refundId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        if (!approve) {
            // 거절
            refund.setRefundStatus(RefundStatus.REJECTED);
            refund.setRefundRejectionReason(rejectionReason);
            refundRequestRepository.save(refund);

            // 거절 이메일 발송
            emailService.sendRefundRejectionEmail(refund.getRequesterEmail(), refund);

            return convertToRefundResponse(refund);
        }

        try {
            // 승인 및 처리
            refund.setRefundStatus(RefundStatus.PROCESSING);
            refundRequestRepository.save(refund);

            // PG사에 환불 요청
            String refundTransactionId = pgGatewayAdapter.processRefund(
                    refund.getPayment().getTransactionId(),
                    refund.getRefundAmount()
            );

            // 환불 완료
            refund.setRefundStatus(RefundStatus.COMPLETED);
            refund.setRefundTransactionId(refundTransactionId);
            refund.setCompletedAt(LocalDateTime.now());
            refundRequestRepository.save(refund);

            // 결제 상태 업데이트
            Payment payment = refund.getPayment();
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);

            // 완료 이메일 발송
            emailService.sendRefundCompletionEmail(refund.getRequesterEmail(), refund);

            notificationService.notifyRefundCompleted(refund);

            return convertToRefundResponse(refund);

        } catch (PgGatewayException e) {
            refund.setRefundStatus(RefundStatus.REJECTED);
            refund.setRefundRejectionReason("PG사 환불 처리 실패: " + e.getMessage());
            refundRequestRepository.save(refund);

            throw new CustomException(ErrorCode.REFUND_FAILED);
        }
    }

    // ... 추가 메서드들
}