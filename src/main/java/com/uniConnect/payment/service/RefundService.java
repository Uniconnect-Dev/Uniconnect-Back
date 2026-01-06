package com.uniConnect.payment.service;

import com.uniConnect.common.service.EmailService;
import com.uniConnect.global.exception.CustomException;
import com.uniConnect.global.exception.ErrorCode;
import com.uniConnect.notification.service.PaymentNotificationServiceImpl;
import com.uniConnect.payment.dto.PaymentDto;
import com.uniConnect.payment.entity.DummyPgGatewayAdapter;
import com.uniConnect.payment.entity.Payment;
import com.uniConnect.payment.entity.RefundRequest;
import com.uniConnect.payment.enums.PaymentStatus;
import com.uniConnect.payment.enums.RefundStatus;
import com.uniConnect.payment.repository.PaymentRepository;
import com.uniConnect.payment.repository.RefundRequestRepository;
import com.uniConnect.common.service.impl.AesEncryptionServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

//refundStatus 추가시: PENDING → PROCESSING → COMPLETED/ REJECTED

/**
 * 환불 서비스
 * - 환불 요청 접수
 * - 환불 승인/거절 (관리자)
 * - 환불 처리 및 완료
 * - 환불 관련 이메일 발송
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RefundService {

    private final RefundRequestRepository refundRequestRepository;
    private final PaymentRepository paymentRepository;
    private final DummyPgGatewayAdapter pgGatewayAdapter;
    private final PaymentNotificationServiceImpl notificationService;
    private final EmailService emailService;
    private final AesEncryptionServiceImpl aesEncryptionServiceImpl;

    /**
     * 1. 환불 요청 생성
     * - 결제된 결제 정보 기반으로 환불 요청
     * - 환불 요청 접수 이메일 발송
     */
    public PaymentDto.RefundResponse requestRefund(Long companyId, PaymentDto.RefundRequestRequest request) {
        log.info("[환불 요청] Company ID: {}, Payment ID: {}", companyId, request.getPaymentId());

        // 1) 결제 정보 조회
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> {
                    log.warn("[환불 요청 실패] 결제 정보 없음 - Payment ID: {}", request.getPaymentId());
                    return new CustomException(ErrorCode.NOT_FOUND);
                });

        // 2) 권한 확인 (회사가 소유한 결제인지)
        if (!payment.getCompany().getCompanyId().equals(companyId)) {
            log.warn("[환불 요청 실패] 권한 없음 - Company ID: {}, Payment Company ID: {}",
                    companyId, payment.getCompany().getCompanyId());
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        return processRefundRequest(payment, request);
    }

    public PaymentDto.RefundResponse requestRefundByStudentOrg(Long studentOrgId, PaymentDto.RefundRequestRequest request) {
        log.info("[환불 요청] StudentOrg ID: {}, Payment ID: {}", studentOrgId, request.getPaymentId());

        // 1) 결제 정보 조회
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> {
                    log.warn("[환불 요청 실패] 결제 정보 없음 - Payment ID: {}", request.getPaymentId());
                    return new CustomException(ErrorCode.NOT_FOUND);
                });

        if (!payment.getStudentOrg().getStudentOrgId().equals(studentOrgId)) {
            log.warn("[환불 요청 실패] 권한 없음 - StudentORg ID: {}", studentOrgId);
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        return processRefundRequest(payment, request);
    }

    /**
     * 공통 환불 요청 처리 로직
     */
    private PaymentDto.RefundResponse processRefundRequest(Payment payment, PaymentDto.RefundRequestRequest request) {
        // 3) 결제 상태 확인 (SUCCESS 또는 REFUNDED 상태만 재환불 가능)
        if (payment.getStatus() != PaymentStatus.SUCCESS && payment.getStatus() != PaymentStatus.REFUNDED) {
            log.warn("[환불 요청 실패] 결제 상태 불가 - Payment ID: {}, Status: {}",
                    request.getPaymentId(), payment.getStatus());
            throw new CustomException(ErrorCode.INVALID_PAYMENT_STATUS);
        }

        // 4) 환불 금액 검증 (결제 금액 이하만 가능)
        if (request.getRefundAmount() <= 0 || request.getRefundAmount() > payment.getAmount()) {
            log.warn("[환불 요청 실패] 환불 금액 초과 - Payment Amount: {}, Refund Amount: {}",
                    payment.getAmount(), request.getRefundAmount());
            throw new CustomException(ErrorCode.INVALID_AMOUNT);
        }

        // 5) 환불 요청 엔티티 생성
        RefundRequest refund = RefundRequest.builder()
                .payment(payment)
                .reason(request.getReason())
                .refundAmount(request.getRefundAmount())
                .refundStatus(RefundStatus.PENDING)  // 초기 상태: PENDING
                .bankName(request.getBankName())
                .accountNoEnc(aesEncryptionServiceImpl.encrypt(request.getAccountNo()))
                .holderName(request.getHolderName())
                .requesterName(request.getRequesterName())
                .requesterEmail(request.getRequesterEmail())
                .requesterPhone(request.getRequesterPhone())
                .requestedAt(LocalDateTime.now())
                .build();

        refundRequestRepository.save(refund);

        log.info("[환불 요청 저장 성공] Refund ID: {}, Amount: {}", refund.getRefundId(), request.getRefundAmount());

        // 6) 환불 요청 접수 이메일 발송
        try {
            emailService.sendRefundRequestConfirmationEmail(refund.getRequesterEmail(), refund);
            log.info("[환불 요청 이메일 발송 성공] Recipient: {}", refund.getRequesterEmail());
        } catch (Exception e) {
            log.error("[환불 요청 이메일 발송 실패] Recipient: {}, Error: {}",
                    refund.getRequesterEmail(), e.getMessage());
            // 이메일 발송 실패는 비즈니스 로직에 영향을 주지 않음
        }

        // 7) 알림 발송
        try {
            notificationService.notifyRefundRequested(refund);
            log.info("[환불 요청 알림 발송 성공] Refund ID: {}", refund.getRefundId());
        } catch (Exception e) {
            log.error("[환불 요청 알림 발송 실패] Refund ID: {}, Error: {}",
                    refund.getRefundId(), e.getMessage());
        }

        return convertToRefundResponse(refund);
    }


    /**
     * 2. 환불 승인 (관리자)
     * - PENDING 상태의 환불 요청을 APPROVED로 변경
     * - PG사에 실제 환불 처리 요청
     * - 환불 완료 이메일 발송
     */
    public PaymentDto.RefundResponse approveRefund(Long refundId) {
        log.info("[환불 승인] Refund ID: {}", refundId);

        RefundRequest refund = refundRequestRepository.findById(refundId)
                .orElseThrow(() -> {
                    log.warn("[환불 승인 실패] 환불 요청 없음 - Refund ID: {}", refundId);
                    return new CustomException(ErrorCode.NOT_FOUND);
                });

        // 1) 환불 상태 확인 (PENDING 상태에서만 승인 가능)
        if (refund.getRefundStatus() != RefundStatus.PENDING) {
            log.warn("[환불 승인 실패] 환불 상태 불가 - Refund ID: {}, Status: {}",
                    refundId, refund.getRefundStatus());
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 2) 상태 변경: PENDING → PROCESSING
        refund.setRefundStatus(RefundStatus.PROCESSING);
        refundRequestRepository.save(refund);
        log.info("[환불 상태 업데이트] Refund ID: {} -> PROCESSING", refundId);

        try {
            // 3) PG사에 환불 요청
            String refundTransactionId = pgGatewayAdapter.processRefund(
                    refund.getPayment().getTransactionId(),
                    refund.getRefundAmount()
            );
            log.info("[PG사 환불 처리 성공] Refund ID: {}, Transaction ID: {}", refundId, refundTransactionId);

            // 4) 환불 완료 처리
            refund.setRefundStatus(RefundStatus.COMPLETED);
            refund.setRefundTransactionId(refundTransactionId);
            refund.setCompletedAt(LocalDateTime.now());
            refundRequestRepository.save(refund);
            log.info("[환불 완료] Refund ID: {}", refundId);

            // 5) 결제 상태 업데이트 (SUCCESS → REFUNDED)
            Payment payment = refund.getPayment();
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
            log.info("[결제 상태 업데이트] Payment ID: {} -> REFUNDED", payment.getPaymentId());

            // 6) 환불 완료 이메일 발송
            try {
                emailService.sendRefundCompletionEmail(refund.getRequesterEmail(), refund);
                log.info("[환불 완료 이메일 발송 성공] Recipient: {}", refund.getRequesterEmail());
            } catch (Exception e) {
                log.error("[환불 완료 이메일 발송 실패] Recipient: {}, Error: {}",
                        refund.getRequesterEmail(), e.getMessage());
            }

            // 7) 알림 발송
            try {
                notificationService.notifyRefundCompleted(refund);
                log.info("[환불 완료 알림 발송 성공] Refund ID: {}", refundId);
            } catch (Exception e) {
                log.error("[환불 완료 알림 발송 실패] Refund ID: {}, Error: {}",
                        refundId, e.getMessage());
            }

            return convertToRefundResponse(refund);

        } catch (Exception e) {
            // PG사 처리 실패 처리
            refund.setRefundStatus(RefundStatus.REJECTED);
            refund.setRefundRejectionReason("PG사 환불 처리 실패: " + e.getMessage());
            refundRequestRepository.save(refund);
            log.error("[PG사 환불 처리 실패] Refund ID: {}, Error: {}", refundId, e.getMessage(), e);

            // 거절 이메일 발송
            try {
                emailService.sendRefundRejectionEmail(refund.getRequesterEmail(), refund);
            } catch (Exception emailError) {
                log.error("[거절 이메일 발송 실패] Error: {}", emailError.getMessage());
            }

            throw new CustomException(ErrorCode.REFUND_FAILED);
        }
    }

    /**
     * 3. 환불 거절 (관리자)
     * - 환불 요청을 거절하고 거절 사유 저장
     * - 거절 이메일 발송
     */
    public PaymentDto.RefundResponse rejectRefund(Long refundId, String rejectionReason) {
        log.info("[환불 거절] Refund ID: {}, Reason: {}", refundId, rejectionReason);

        RefundRequest refund = refundRequestRepository.findById(refundId)
                .orElseThrow(() -> {
                    log.warn("[환불 거절 실패] 환불 요청 없음 - Refund ID: {}", refundId);
                    return new CustomException(ErrorCode.NOT_FOUND);
                });

        // 1) 환불 상태 확인 (PENDING 상태에서만 거절 가능)
        if (refund.getRefundStatus() != RefundStatus.PENDING) {
            log.warn("[환불 거절 실패] 환불 상태 불가 - Refund ID: {}, Status: {}",
                    refundId, refund.getRefundStatus());
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 2) 거절 처리
        refund.setRefundStatus(RefundStatus.REJECTED);
        refund.setRefundRejectionReason(rejectionReason);
        refund.setRejectedAt(LocalDateTime.now());
        refundRequestRepository.save(refund);
        log.info("[환불 거절 저장 성공] Refund ID: {}", refundId);

        // 3) 거절 이메일 발송
        try {
            emailService.sendRefundRejectionEmail(refund.getRequesterEmail(), refund);
            log.info("[거절 이메일 발송 성공] Recipient: {}", refund.getRequesterEmail());
        } catch (Exception e) {
            log.error("[거절 이메일 발송 실패] Recipient: {}, Error: {}",
                    refund.getRequesterEmail(), e.getMessage());
        }

        return convertToRefundResponse(refund);
    }

    /**
     * 4. 환불 요청 조회 (단일)
     */
    @Transactional(readOnly = true)
    public PaymentDto.RefundResponse getRefund(Long refundId, Long companyId) {
        log.info("[환불 조회] Refund ID: {}, Company ID: {}", refundId, companyId);

        RefundRequest refund = refundRequestRepository.findById(refundId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        // 권한 확인
        if (!refund.getPayment().getCompany().getCompanyId().equals(companyId)) {
            log.warn("[환불 조회 실패] 권한 없음 - Company ID: {}", companyId);
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        return convertToRefundResponse(refund);
    }

    @Transactional(readOnly = true)
    public PaymentDto.RefundResponse getRefundByStudentOrg(Long refundId, Long studentOrgId) {
        log.info("[환불 조회] Refund ID: {}, StudentOrg ID: {}", refundId, studentOrgId);

        RefundRequest refund = refundRequestRepository.findById(refundId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        if (!refund.getPayment().getStudentOrg().getStudentOrgId().equals(studentOrgId)) {
            log.warn("[환불 조회 실패] 권한 없음 - StudentOrg ID: {}", studentOrgId);
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        return convertToRefundResponse(refund);
    }

    @Transactional(readOnly = true)
    public List<PaymentDto.RefundResponse> getRefundsByCompany(Long companyId) {
        log.info("[환불 목록 조회] Company ID: {}", companyId);

        // 회사 존재 여부 확인
        // companyRepository.findById(companyId)
        //         .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        // 해당 기업의 모든 결제와 관련된 환불 요청 조회
        List<RefundRequest> refunds = refundRequestRepository.findByPaymentCompanyCompanyIdOrderByRequestedAtDesc(companyId);

        return refunds.stream()
                .map(this::convertToRefundResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentDto.RefundResponse> getRefundsByStudentOrg(Long studentOrgId) {
        log.info("[환불 목록 조회] StudentOrg ID: {}", studentOrgId);

        List<RefundRequest> refunds = refundRequestRepository.findByPaymentStudentOrgStudentOrgIdOrderByRequestedAtDesc(studentOrgId);

        return refunds.stream()
                .map(this::convertToRefundResponse)
                .collect(Collectors.toList());
    }

    /**
     * 5. DTO 변환 헬퍼 메서드
     */
    private PaymentDto.RefundResponse convertToRefundResponse(RefundRequest refund) {
        return PaymentDto.RefundResponse.builder()
                .refundId(refund.getRefundId())
                .paymentId(refund.getPayment().getPaymentId())
                .refundAmount(refund.getRefundAmount())
                .refundStatus(refund.getRefundStatus())
                .reason(refund.getReason())
                .refundRejectionReason(refund.getRefundRejectionReason())
                .bankName(refund.getBankName())
                .holderName(refund.getHolderName())
                .requesterName(refund.getRequesterName())
                .requesterEmail(refund.getRequesterEmail())
                .requesterPhone(refund.getRequesterPhone())
                .requestedAt(refund.getRequestedAt())
                .completedAt(refund.getCompletedAt())
                .refundTransactionId(refund.getRefundTransactionId())
                .build();
    }

    /**
     * 계좌번호 마스킹 (예: 123456-789456-123456 → 123456-****-123456)
     */
    private String maskAccountNumber(String encryptedAccountNo) {
        try {
            // 암호화된 계좌번호를 복호화
            String decrypted = aesEncryptionServiceImpl.decrypt(encryptedAccountNo);

            if (decrypted == null || decrypted.length() < 8) {
                return "****";
            }

            int len = decrypted.length();
            int maskStart = len / 3;
            int maskEnd = (len * 2) / 3;

            StringBuilder masked = new StringBuilder(decrypted);
            for (int i = maskStart; i < maskEnd; i++) {
                masked.setCharAt(i, '*');
            }

            log.debug("[계좌번호 마스킹 성공] 원본 길이: {}, 마스킹: {}", len, masked);
            return masked.toString();

        } catch (Exception e) {
            log.error("[계좌번호 마스킹 실패] Error: {}", e.getMessage());
            return "****";
        }
    }
}