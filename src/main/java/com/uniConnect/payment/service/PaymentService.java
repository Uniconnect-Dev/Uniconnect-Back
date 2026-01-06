package com.uniConnect.payment.service;

import com.uniConnect.global.exception.CustomException;
import com.uniConnect.global.exception.ErrorCode;
import com.uniConnect.matching.entity.CollaborationMatchRequest;
import com.uniConnect.matching.repository.CollaborationMatchRequestRepository;
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
    private final CollaborationMatchRequestRepository collaborationMatchRequestRepository;

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
     * 2. 결제 진행 (CollabMatchReq 결제)
     */
    //바뀌는 로직부분만 분리
    public PaymentDto.PaymentListResponse createPayment(Long companyId, PaymentDto.PaymentCreateRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        return processPaymentByMatchRequest(company, null, request);
    }
    public PaymentDto.PaymentListResponse createPaymentByStudentOrg(Long studentOrgId, PaymentDto.PaymentCreateRequest request) {
        StudentOrg studentOrg = studentOrgRepository.findById(studentOrgId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        return processPaymentByMatchRequest(null, studentOrg, request);
    }

    //null값으로 method overload
    private PaymentDto.PaymentListResponse processPaymentByMatchRequest(
            Company company,
            StudentOrg studentOrg,
            PaymentDto.PaymentCreateRequest request) {

        // 1) CollaborationMatchRequest 조회
        CollaborationMatchRequest matchRequest= collaborationMatchRequestRepository
                .findById(request.getCollaborationMatchRequestId())
                .orElseThrow(()-> new CustomException(ErrorCode.NOT_FOUND));

        // 2) 권한 확인
        if (company != null) {
            if (!matchRequest.getCompany().getCompanyId().equals(company.getCompanyId())) {
                throw new CustomException(ErrorCode.UNAUTHORIZED);
            }
            log.info("[결제 진행] Company ID: {}, MatchRequest ID: {}", company.getCompanyId(), matchRequest.getId());
        } else {
            if (!matchRequest.getStudentOrg().getStudentOrgId().equals(studentOrg.getStudentOrgId())) {
                throw new CustomException(ErrorCode.UNAUTHORIZED);
            }
            log.info("[결제 진행] StudentOrg ID: {}, MatchRequest ID: {}", studentOrg.getStudentOrgId(), matchRequest.getId());
        }

        // 3) 결제 수단 조회
        PaymentMethod method = paymentMethodRepository.findById(request.getPaymentMethodId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        // 4) 결제 수단 권한 확인
        if (company != null && !method.getCompany().getCompanyId().equals(company.getCompanyId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        if (studentOrg != null && !method.getStudentOrg().getStudentOrgId().equals(studentOrg.getStudentOrgId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

//        // 5) 금액 검증
//        Long matchRequestAmount = matchRequest.getExpectedCost();  // CollaborationMatchRequest의 예상 비용
//        if (!request.getAmount().equals(matchRequestAmount)) {
//            throw new CustomException(ErrorCode.INVALID_AMOUNT);
//        }

        // 6) Payment 엔티티 생성
        Payment payment = Payment.builder()
                .company(company)
                .studentOrg(studentOrg)
                .collaborationMatchRequest(matchRequest)
                .amount(request.getAmount().intValue())
                .method(method)
                .status(PaymentStatus.PROCESSING)
                .build();
        paymentRepository.save(payment);

        try {
            // 7) PG사에 결제 요청
            String transactionId = pgGatewayAdapter.processPayment(
                    payment.getPaymentId(),
                    request.getAmount(),
                    method
            );

            // 8) 결제 성공 업데이트
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setTransactionId(transactionId);
            payment.setCompletedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            log.info("[결제 성공] Payment ID: {}, TransactionId: {}", payment.getPaymentId(), transactionId);

            // 9) 알림 발송
            notificationService.notifyPaymentSuccess(payment);

//            // 10) 세금계산서/영수증 자동 발행
//            issueInvoiceAfterPayment(payment);
//
            return convertToPaymentListResponse(payment);

        } catch (Exception e) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);

            log.error("[결제 실패] Payment ID: {}, Error: {}", payment.getPaymentId(), e.getMessage(), e);
            notificationService.notifyPaymentFailed(payment);
            throw new CustomException(ErrorCode.PAYMENT_FAILED);
        }
    }

//    private void issueInvoiceAfterPayment(Payment payment) {
//        try {
//            CollaborationMatchRequest matchRequest = payment.getCollaborationMatchRequest();
//
//            // Invoice 생성
//            Invoice invoice = Invoice.builder()
//                    .issueType(InvoiceIssueType.Tax)  // 기본값: 세금계산서
//                    .status(InvoiceStatus.PENDING)
//                    .amount(payment.getAmount().longValue())
//                    .taxAmount(payment.getAmount() / 10L)  // 10% 세액
//                    .companyName(matchRequest.getCompany().getName())
//                    .representativeName(matchRequest.getCompany().getName())  // 필요시 조회
//                    .address(matchRequest.getCompany().getName())
//                    .bizType("협업")
//                    .bizItem(matchRequest.getProposalContent() != null ? matchRequest.getProposalContent().substring(0, 50) : "협업")
//                    .email(matchRequest.getCompany().getName())
//                    .payment(payment)
//                    .company(payment.getCompany())
//                    .studentOrg(payment.getStudentOrg())
//                    .build();
//
//            invoiceRepository.save(invoice);
//
//            log.info("✅ Invoice 자동 생성: Payment ID = {}, Invoice ID = {}",
//                    payment.getPaymentId(), invoice.getInvoiceId());
//
//        } catch (Exception e) {
//            log.error("❌ Invoice 자동 생성 실패: {}", e.getMessage(), e);
//            // Invoice 생성 실패는 결제 롤백하지 않음
//        }
//    }

    /**
     * 3. 결제 취소 (PROCESSING 상태에서만 가능)
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
        CollaborationMatchRequest matchRequest = payment.getCollaborationMatchRequest();
        String methodType = payment.getMethod() != null ? payment.getMethod().getType().toString() : "N/A";

        PaymentDto.PaymentListResponse.PaymentListResponseBuilder builder =
                PaymentDto.PaymentListResponse.builder()
                        .paymentId(payment.getPaymentId())
                        .amount(payment.getAmount())
                        .status(payment.getStatus())
                        .transactionId(payment.getTransactionId())
                        .receiptUrl(payment.getReceiptUrl())
                        .createdAt(payment.getCreatedAt())
                        .completedAt(payment.getCompletedAt())
                        .canceledAt(payment.getCanceledAt())
                        .paymentMethodType(methodType);

        // CollaborationMatchRequest 정보 추가
        builder.matchRequestId(matchRequest.getId() != null ? matchRequest.getId() : null)
                .matchRequestType(matchRequest.getCollaborationType() != null
                        ? matchRequest.getCollaborationType().toString() : "N/A");

        return builder.build();
    }
}