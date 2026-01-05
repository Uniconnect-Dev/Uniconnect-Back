package com.uniConnect.payment.service;

import com.uniConnect.global.exception.CustomException;
import com.uniConnect.global.exception.ErrorCode;
import com.uniConnect.notification.service.PaymentNotificationServiceImpl;
import com.uniConnect.payment.dto.*;
import com.uniConnect.payment.entity.DummyPgGatewayAdapter;
import com.uniConnect.payment.entity.Payment;
import com.uniConnect.payment.entity.PaymentMethod;
import com.uniConnect.payment.enums.PaymentStatus;
import com.uniConnect.payment.repository.*;
import com.uniConnect.campaign.repository.CampaignRepository;
import com.uniConnect.campaign.entity.Campaign;
import com.uniConnect.company.repository.CompanyRepository;
import com.uniConnect.company.entity.Company;
import com.uniConnect.studentOrg.entity.StudentOrg;
import com.uniConnect.studentOrg.repository.StudentOrgRepository;
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
    private final StudentOrgRepository studentOrgRepository;
    private final DummyPgGatewayAdapter pgGatewayAdapter;
    private final PaymentNotificationServiceImpl notificationService;

    /**
     * 1. 기업의 모든 결제 내역 조회
     */
    @Transactional(readOnly = true)
    public List<PaymentDto.PaymentListResponse> getPayments(Long companyId) {
        // 회사 존재 여부 확인
        companyRepository.findById(companyId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        log.info("[결제 내역 조회] Company ID: {}", companyId);

        return paymentRepository.findByCompanyCompanyIdOrderByCreatedAtDesc(companyId)
                .stream()
                .map(this::convertToPaymentListResponse)
                .collect(Collectors.toList());
    }

    /**
     * 1-2. 학생단체별 결제 내역 조회
     */
    @Transactional(readOnly = true)
    public List<PaymentDto.PaymentListResponse> getPaymentsByStudentOrg(Long studentOrgId) {
        studentOrgRepository.findById(studentOrgId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        return paymentRepository.findByStudentOrgStudentOrgIdOrderByCreatedAtDesc(studentOrgId)
                .stream()
                .map(this::convertToPaymentListResponse)
                .collect(Collectors.toList());
    }

    /**
     * 2. 결제 진행 (캠페인 결제)
     */
    //바뀌는 로직부분만 분리
    public PaymentDto.PaymentListResponse createPayment(Long companyId, PaymentDto.PaymentCreateRequest request) {
        // 1) 회사 정보 조회
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        return processPayment(company, null, request);
    }

    public PaymentDto.PaymentListResponse createPaymentByStudentOrg(Long studentOrgId, PaymentDto.PaymentCreateRequest request) {
        StudentOrg studentOrg = studentOrgRepository.findById(studentOrgId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        return processPayment(null, studentOrg, request);
    }

    //null값으로 method overload
    private PaymentDto.PaymentListResponse processPayment(Company company, StudentOrg studentOrg, PaymentDto.PaymentCreateRequest request) {
        // 2) 캠페인 정보 조회
        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        // 3) 회사 or 학생단체 권한 확인
        if (company != null) {
            if (!campaign.getCompany().getCompanyId().equals(company.getCompanyId())) {
                throw new CustomException(ErrorCode.UNAUTHORIZED);
            }
            log.info("[결제 진행] Company ID: {}", company.getCompanyId());
        } else {
            if (!campaign.getStudentOrg().getStudentOrgId().equals(studentOrg.getStudentOrgId())) {
                throw new CustomException(ErrorCode.UNAUTHORIZED);
            }
            log.info("[결제 진행] StudentOrg ID: {}", studentOrg.getStudentOrgId());
        }

        // 4) 결제 수단 조회
        PaymentMethod method = paymentMethodRepository.findById(request.getPaymentMethodId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        // 5) 결제 수단 소유 권한 확인
        if (company != null && !method.getCompany().getCompanyId().equals(company.getCompanyId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        if (studentOrg != null && !method.getStudentOrg().getStudentOrgId().equals(studentOrg.getStudentOrgId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // 6) 금액 검증
        if (!request.getAmount().equals(campaign.getTotalCost())) {
            throw new CustomException(ErrorCode.INVALID_AMOUNT);
        }

        // 7) Payment 엔티티 생성
        Payment payment = Payment.builder()
                .company(company)
                .studentOrg(studentOrg)
                .campaign(campaign)
                .amount(request.getAmount())
                .method(method)
                .status(PaymentStatus.PROCESSING)
                .build();

        paymentRepository.save(payment);

        log.info("[결제 시작] Payment ID: {}, Campaign ID: {}, Amount: {}",
                payment.getPaymentId(), campaign.getCampaignId(), request.getAmount());

        try {
            // 8) PG사에 결제 요청
            String transactionId = pgGatewayAdapter.processPayment(
                    payment.getPaymentId(),
                    request.getAmount(),
                    method
            );

            // 9) 결제 성공 업데이트
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setTransactionId(transactionId);
            payment.setCompletedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            log.info("[결제 성공] Payment ID: {}, TransactionId: {}", payment.getPaymentId(), transactionId);

            // 10) 알림 발송
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

        if (!payment.getCompany().getCompanyId().equals(companyId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        performCancelPayment(payment);
    }

    public void cancelPaymentByStudentOrg(Long paymentId, Long studentOrgId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        if (!payment.getStudentOrg().getStudentOrgId().equals(studentOrgId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        performCancelPayment(payment);
    }

    /**
     * 공통 결제 취소 로직
     */
    private void performCancelPayment(Payment payment) {
        if (payment.getStatus() != PaymentStatus.PROCESSING) {
            throw new CustomException(ErrorCode.INVALID_PAYMENT_STATUS);
        }

        payment.setStatus(PaymentStatus.CANCELED);
        payment.setCanceledAt(LocalDateTime.now());
        paymentRepository.save(payment);

        log.info("[결제 취소] Payment ID: {}", payment.getPaymentId());
        notificationService.notifyPaymentFailed(payment);
    }


    /**
     * 6. 결제 재시도 (FAILED 상태에서만 가능)
     */
    public PaymentDto.PaymentListResponse retryPayment(Long paymentId, Long companyId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        if (!payment.getCompany().getCompanyId().equals(companyId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        return performRetryPayment(payment);
    }

    public PaymentDto.PaymentListResponse retryPaymentByStudentOrg(Long paymentId, Long studentOrgId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        if (!payment.getStudentOrg().getStudentOrgId().equals(studentOrgId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        return performRetryPayment(payment);
    }

    /**
     * 공통 결제 재시도 로직
     */
    private PaymentDto.PaymentListResponse performRetryPayment(Payment payment) {
        if (payment.getStatus() != PaymentStatus.FAILED) {
            throw new CustomException(ErrorCode.INVALID_PAYMENT_STATUS);
        }

        payment.setStatus(PaymentStatus.PROCESSING);
        paymentRepository.save(payment);

        log.info("[결제 재시도] Payment ID: {}", payment.getPaymentId());

        try {
            String transactionId = pgGatewayAdapter.processPayment(
                    payment.getPaymentId(),
                    payment.getAmount(),
                    payment.getMethod()
            );

            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setTransactionId(transactionId);
            payment.setCompletedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            log.info("[결제 재시도 성공] Payment ID: {}, TransactionId: {}", payment.getPaymentId(), transactionId);
            notificationService.notifyPaymentSuccess(payment);

            return convertToPaymentListResponse(payment);

        } catch (Exception e) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            log.error("[결제 재시도 실패] Payment ID: {}, Error: {}", payment.getPaymentId(), e.getMessage(), e);
            throw new CustomException(ErrorCode.PAYMENT_FAILED);
        }
    }

    /**
     * 7. DTO 변환 헬퍼 메서드
     */
    private PaymentDto.PaymentListResponse convertToPaymentListResponse(Payment payment) {
        String campaignName = payment.getCampaign() != null ? payment.getCampaign().getName() : "N/A";
        String methodType = payment.getMethod() != null ? payment.getMethod().getType().toString() : "N/A";

        return PaymentDto.PaymentListResponse.builder()
                .paymentId(payment.getPaymentId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())  // ← 추가
                .receiptUrl(payment.getReceiptUrl())  // ← 추가
                .createdAt(payment.getCreatedAt())
                .completedAt(payment.getCompletedAt())  // ← 추가
                .canceledAt(payment.getCanceledAt())  // ← 추가
                .createdAt(payment.getCreatedAt())
                .campaignName(campaignName)
                .campaignAmount(payment.getAmount())
                .paymentMethodType(methodType)
                .build();
    }
}