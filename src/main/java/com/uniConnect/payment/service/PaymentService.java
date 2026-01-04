package com.uniConnect.payment.service;

import com.uniConnect.global.exception.CustomException;
import com.uniConnect.global.exception.ErrorCode;
import com.uniConnect.notification.service.PaymentNotificationServiceImpl;
import com.uniConnect.payment.dto.*;
import com.uniConnect.payment.entity.DummyPgGatewayAdapter;
import com.uniConnect.payment.entity.Payment;
import com.uniConnect.payment.entity.PaymentMethod;
import com.uniConnect.payment.entity.PgGatewayAdapter;
import com.uniConnect.payment.enums.PaymentStatus;
import com.uniConnect.payment.repository.*;
import com.uniConnect.campaign.repository.CampaignRepository;
import com.uniConnect.campaign.entity.Campaign;
import com.uniConnect.company.repository.CompanyRepository;
import com.uniConnect.company.entity.Company;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final CampaignRepository campaignRepository;
    private final CompanyRepository companyRepository;
    private final DummyPgGatewayAdapter pgGatewayAdapter;
    private final PaymentNotificationServiceImpl notificationService;

    /**
     * 1. 기업의 모든 결제 내역 조회
     */
    @Transactional(readOnly = true)
    public List<PaymentDto.PaymentListResponse> getPaymentsByCompany(Long companyId) {
        // 회사 존재 여부 확인
        companyRepository.findById(companyId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        log.info("[결제 내역 조회] Company ID: {}", companyId);

        return paymentRepository.findByCompanyCompanyIdOrderByCreatedAtDesc(companyId)
                .stream()
                .map(this::convertToPaymentListResponse)
                .collect(Collectors.toList());
    }

//    /**
//     * 2. 캠페인별 결제 내역 조회
//     */
//    @Transactional(readOnly = true)
//    public List<PaymentDto.PaymentListResponse> getPaymentsByCampaign(Long campaignId) {
//        // 캠페인 존재 여부 확인
//        campaignRepository.findById(campaignId)
//                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
//
//        log.info("[캠페인별 결제 조회] Campaign ID: {}", campaignId);
//
//        return paymentRepository.findByCampaignCampaignIdOrderByCreatedAtDesc(campaignId)
//                .stream()
//                .map(this::convertToPaymentListResponse)
//                .collect(Collectors.toList());
//    }

//    /**
//     * 3. 단일 결제 내역 조회
//     */
//    @Transactional(readOnly = true)
//    public PaymentDto.PaymentListResponse getPaymentDetails(Long paymentId, Long companyId) {
//        Payment payment = paymentRepository.findById(paymentId)
//                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
//
//        // 권한 확인
//        if (!payment.getCompany().getCompanyId().equals(companyId)) {
//            throw new CustomException(ErrorCode.UNAUTHORIZED);
//        }
//
//        return convertToPaymentListResponse(payment);
//    }

    /**
     * 4. 결제 진행 (캠페인 결제)
     */
    public PaymentDto.PaymentListResponse createPayment(Long companyId, PaymentDto.PaymentCreateRequest request) {
        // 1) 회사 정보 조회
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        // 2) 캠페인 정보 조회
        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        // 3) 회사 권한 확인
        if (!campaign.getCompany().getCompanyId().equals(companyId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // 4) 결제 수단 조회
        PaymentMethod method = paymentMethodRepository.findById(request.getPaymentMethodId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        // 5) 결제 수단 소유 권한 확인
        if (!method.getCompany().getCompanyId().equals(companyId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // 6) 금액 검증
        if (!request.getAmount().equals(campaign.getTotalCost())) {
            throw new CustomException(ErrorCode.INVALID_AMOUNT);
        }

//        // 7) 이미 결제되었는지 확인
//        paymentRepository.findSuccessfulPaymentByCampaign(campaign.getCampaignId())
//                .ifPresent(p -> {
//                    throw new CustomException(ErrorCode.PAYMENT_ALREADY_PROCESSED);
//                });

        // 8) Payment 엔티티 생성
        Payment payment = Payment.builder()
                .company(company)
                .campaign(campaign)
                .amount(request.getAmount())
                .method(method)
                .status(PaymentStatus.PROCESSING)
                .build();

        paymentRepository.save(payment);

        log.info("[결제 시작] Payment ID: {}, Company ID: {}, Campaign ID: {}, Amount: {}",
                payment.getPaymentId(), companyId, campaign.getCampaignId(), request.getAmount());

        try {
            // 9) PG사에 결제 요청
            String transactionId = pgGatewayAdapter.processPayment(
                    payment.getPaymentId(),
                    request.getAmount(),
                    method
            );

            // 10) 결제 성공 업데이트
            payment.setStatus(PaymentStatus.SUCCESS);
//            payment.setTransactionId(transactionId);
            paymentRepository.save(payment);

            log.info("[결제 성공] Payment ID: {}, TransactionId: {}", payment.getPaymentId(), transactionId);

            // 11) 알림 발송
            notificationService.notifyPaymentSuccess(payment);

            return convertToPaymentListResponse(payment);

        } catch (Exception e) {
            // 결제 실패 처리
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);

            log.error("[결제 실패] Payment ID: {}, Error: {}", payment.getPaymentId(), e.getMessage(), e);

            notificationService.notifyPaymentFailed(payment);
            throw new CustomException(ErrorCode.PAYMENT_FAILED);
        }
    }

    /**
     * 5. 결제 취소 (PROCESSING 상태에서만 가능)
     */
    public void cancelPayment(Long paymentId, Long companyId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        // 권한 확인
        if (!payment.getCompany().getCompanyId().equals(companyId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // PROCESSING 상태에서만 취소 가능
        if (payment.getStatus() != PaymentStatus.PROCESSING) {
            throw new CustomException(ErrorCode.INVALID_PAYMENT_STATUS);
        }

        payment.setStatus(PaymentStatus.CANCELED);
        paymentRepository.save(payment);

        log.info("[결제 취소] Payment ID: {}, Company ID: {}", paymentId, companyId);

        notificationService.notifyPaymentFailed(payment);
    }

    /**
     * 6. 결제 재시도 (FAILED 상태에서만 가능)
     */
    public PaymentDto.PaymentListResponse retryPayment(Long paymentId, Long companyId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        // 권한 확인
        if (!payment.getCompany().getCompanyId().equals(companyId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // FAILED 상태에서만 재시도 가능
        if (payment.getStatus() != PaymentStatus.FAILED) {
            throw new CustomException(ErrorCode.INVALID_PAYMENT_STATUS);
        }

        payment.setStatus(PaymentStatus.PROCESSING);
        paymentRepository.save(payment);

        log.info("[결제 재시도] Payment ID: {}, Company ID: {}", paymentId, companyId);

        try {
            // PG사에 결제 재요청
            String transactionId = pgGatewayAdapter.processPayment(
                    payment.getPaymentId(),
                    payment.getAmount(),
                    payment.getMethod()
            );

            // 결제 성공 업데이트
            payment.setStatus(PaymentStatus.SUCCESS);
//            payment.setTransactionId(transactionId);
            paymentRepository.save(payment);

            log.info("[결제 재시도 성공] Payment ID: {}, TransactionId: {}", paymentId, transactionId);

            notificationService.notifyPaymentSuccess(payment);

            return convertToPaymentListResponse(payment);

        } catch (Exception e) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);

            log.error("[결제 재시도 실패] Payment ID: {}, Error: {}", paymentId, e.getMessage(), e);

            throw new CustomException(ErrorCode.PAYMENT_FAILED);
        }
    }

//    /**
//     * 7. 결제 현황 통계 (기업별)
//     */
//    @Transactional(readOnly = true)
//    public PaymentDto.PaymentStatisticsResponse getPaymentStatistics(Long companyId) {
//        companyRepository.findById(companyId)
//                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
//
//        Optional<Payment> allPayments = paymentRepository.findByCompanyCompanyIdOrderByCreatedAtDesc(companyId);
//
//        long totalAmount = allPayments.stream()
//                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
//                .mapToLong(Payment::getAmount)
//                .sum();
//
//        long successCount = allPayments.stream()
//                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
//                .count();
//
//        long failedCount = allPayments.stream()
//                .filter(p -> p.getStatus() == PaymentStatus.FAILED)
//                .count();
//
//        log.info("[결제 통계] Company ID: {}, Total: {}, Success: {}, Failed: {}",
//                companyId, totalAmount, successCount, failedCount);
//
//        return PaymentDto.PaymentStatisticsResponse.builder()
//                .totalAmount(totalAmount)
//                .successCount(successCount)
//                .failedCount(failedCount)
//                .cancelledCount(allPayments.stream().filter(p -> p.getStatus() == PaymentStatus.CANCELLED).count())
//                .processingCount(allPayments.stream().filter(p -> p.getStatus() == PaymentStatus.PROCESSING).count())
//                .build();
//    }

    /**
     * 8. DTO 변환 헬퍼 메서드
     */
    private PaymentDto.PaymentListResponse convertToPaymentListResponse(Payment payment) {
        String campaignName = payment.getCampaign() != null ? payment.getCampaign().getName() : "N/A";
        String methodType = payment.getMethod() != null ? payment.getMethod().getType().toString() : "N/A";

        return PaymentDto.PaymentListResponse.builder()
                .paymentId(payment.getPaymentId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .campaignName(campaignName)
                .campaignAmount(payment.getAmount())
                .paymentMethodType(methodType)
//                .receiptUrl(payment.getReceiptUrl())
                .build();
    }
}