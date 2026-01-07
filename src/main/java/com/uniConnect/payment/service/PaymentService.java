package com.uniConnect.payment.service;

import com.uniConnect.company.entity.CompanyContact;
import com.uniConnect.company.repository.CompanyContactRepository;
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
    private final CompanyRepository companyRepository;
    private final CompanyContactRepository companyContactRepository;
    private final StudentOrgRepository studentOrgRepository;
    private final DummyPgGatewayAdapter pgGatewayAdapter;
    private final PaymentNotificationServiceImpl notificationService;
    private final CollaborationMatchRequestRepository collaborationMatchRequestRepository;
    private final InvoiceRequestService invoiceRequestService;

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
    //초기: company, studentOrg 나눠관리
    public PaymentDto.PaymentListResponse createPayment(PaymentDto.PaymentCreateRequest request) {
        CollaborationMatchRequest matchRequest= collaborationMatchRequestRepository
                .findById(request.getCollaborationMatchRequestId())
                .orElseThrow(()->new CustomException(ErrorCode.NOT_FOUND));

        return processPaymentByMatchRequest(matchRequest, request);
    }

    //null값으로 method overload
    private PaymentDto.PaymentListResponse processPaymentByMatchRequest(
            CollaborationMatchRequest matchRequest,
            PaymentDto.PaymentCreateRequest request) {

        // 2) 권한 확인
        validateMatchRequestPermission(matchRequest, request.getRequesterId(), request.getRequesterType());

        // 3) 결제 수단 조회, 권한 확인
        PaymentMethod method = paymentMethodRepository.findById(request.getPaymentMethodId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        validatePaymentMethodPermission(method, matchRequest);

        // 4) Payment 엔티티 생성
        Payment payment = Payment.builder()
                .company(matchRequest.getCompany())
                .studentOrg(matchRequest.getStudentOrg())
                .collaborationMatchRequest(matchRequest)
                .amount(request.getAmount())
                .method(method)
                .status(PaymentStatus.PROCESSING)
                .build();
        paymentRepository.save(payment);

        try {
            // 5) PG사에 결제 요청
            String transactionId = pgGatewayAdapter.processPayment(
                    payment.getPaymentId(),
                    request.getAmount(),
                    method
            );

            // 6) 결제 성공 업데이트
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setTransactionId(transactionId);
            payment.setCompletedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            log.info("[결제 성공] Payment ID: {}, TransactionId: {}", payment.getPaymentId(), transactionId);

            // 7) 알림 발송
            notificationService.notifyPaymentSuccess(payment);

            // 8) 세금계산서/영수증 자동 발행
            issueInvoiceAfterPayment(payment);

            return convertToPaymentListResponse(payment);

        } catch (Exception e) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);

            log.error("[결제 실패] Payment ID: {}, Error: {}", payment.getPaymentId(), e.getMessage(), e);
            notificationService.notifyPaymentFailed(payment);
            throw new CustomException(ErrorCode.PAYMENT_FAILED);
        }
    }

    /**
     * 권한 확인: 결제 요청자가 CollabMatchReq에 속하는지 확인, 결제 수단 권한 확인
     */
    private void validateMatchRequestPermission(CollaborationMatchRequest matchRequest, Long requesterId, String requesterType) {
        if ("COMPANY".equalsIgnoreCase(requesterType)) {
            if (!matchRequest.getCompany().getCompanyId().equals(requesterId)) {
                throw new CustomException(ErrorCode.UNAUTHORIZED);
            }
        }
        else if ("STUDENT_ORG".equalsIgnoreCase(requesterType) || "STUDENTORG".equalsIgnoreCase(requesterType)) {
            if (!matchRequest.getStudentOrg().getStudentOrgId().equals(requesterId)) {
                throw new CustomException(ErrorCode.UNAUTHORIZED);
            }
        }
        else throw new CustomException(ErrorCode.UNAUTHORIZED);
    }

    private void validatePaymentMethodPermission(PaymentMethod method, CollaborationMatchRequest matchRequest) {
        Company company = matchRequest.getCompany();
        StudentOrg studentOrg = matchRequest.getStudentOrg();

        if (company != null && method.getCompany() != null) {
            if (!method.getCompany().getCompanyId().equals(company.getCompanyId())) {
                throw new CustomException(ErrorCode.UNAUTHORIZED);
            }
        } else if (studentOrg != null && method.getStudentOrg() != null) {
            if (!method.getStudentOrg().getStudentOrgId().equals(studentOrg.getStudentOrgId())) {
                throw new CustomException(ErrorCode.UNAUTHORIZED);
            }
        } else {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
    }

    /**
     * InvoiceRequest 자동 생성 (결제 성공 후)
     */
    private void issueInvoiceAfterPayment(Payment payment) {
        try {
            CompanyContact companycontact = companyContactRepository.findMainContactByCompany(payment.getCompany(), payment.getCompany().getMainContactId())
                    .orElseThrow(()->new CustomException(ErrorCode.NOT_FOUND));
            Company company = payment.getCompany();
            StudentOrg studentOrg = payment.getStudentOrg();
            PaymentMethod method = payment.getMethod();
            Long userId = company != null ? company.getCompanyId(): studentOrg.getStudentOrgId();

            // 부가세 계산 (10%)
            Long taxAmount = (long) (payment.getAmount() * 0.1);

            PaymentDto.InvoiceCreateRequest dto = PaymentDto.InvoiceCreateRequest.builder()
                    .paymentId(payment.getPaymentId())
                    .paymentDateTime(payment.getCompletedAt())
                    .paymentMethod(method.getType())
                    .customerName(company != null ? company.getBrandName() : studentOrg.getSchoolName())
                    .email(company != null ? companycontact.getEmail() : studentOrg.getEmail())
                    .phone(company != null ? companycontact.getPhone() : studentOrg.getPhone())
                    .cardNumber(maskingCardNumber(method))  // 카드번호 마스킹
                    .approvalNumber(payment.getTransactionId())  // 승인번호
                    .originalAmount(payment.getAmount().longValue())  // 최초금액
                    .discountAmount(0L)  // 할인 (기본값 0)
                    .taxAmount(taxAmount)  // 부가세 (10%)
                    .partnershipFee(0L)  // 제휴수수료 (기본값 0)
                    .additionalMarketing(0L)  // 추가마케팅 (기본값 0)
                    .totalAmount(payment.getAmount().longValue())  // 총결제금액
                    .netAmount(payment.getAmount().longValue())  // 순수금액
                    .bizNumber("")  // Business Registration에서 조회 필요
                    .companyName(company != null ? company.getBrandName() : studentOrg.getSchoolName())
                    .representativeName(company != null ? company.getBrandName() : studentOrg.getSchoolName())
                    .bizType("협업")
                    .bizItem("마케팅 협업")
                    .userId(userId)
                    .build();

            invoiceRequestService.createInvoiceRequestFromPayment(dto);

            log.info("✅ InvoiceRequest 생성 완료: PaymentId={}, InvoiceRequestUserId={}",
                    payment.getPaymentId(), userId);

        } catch (Exception e) {
            log.error("❌ InvoiceRequest 생성 실패: {}", e.getMessage(), e);
            // InvoiceRequest 생성 실패는 결제 롤백하지 않음
        }
    }

    private String maskingCardNumber(PaymentMethod method) {
        try {
            String cardNumber = method.getCardNumberEnc();
            if (cardNumber != null && cardNumber.length() >= 4) {
                return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
            }
            return "****";
        } catch (Exception e) {
            return "****";
        }
    }

    /**
     * 3. 결제 취소 (PROCESSING 상태에서만 가능)
     */
    public void cancelPayment(Long paymentId, Long requesterId, String requesterType) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        validatePaymentOwnership(payment, requesterId, requesterType);

        if (payment.getStatus() != PaymentStatus.PROCESSING) { //processing일때만
            throw new CustomException(ErrorCode.INVALID_PAYMENT_STATUS);
        }

        payment.setStatus(PaymentStatus.CANCELED);
        payment.setCanceledAt(LocalDateTime.now());
        paymentRepository.save(payment);

        log.info("[결제 취소] Payment ID: {}", payment.getPaymentId());
        notificationService.notifyPaymentFailed(payment);
    }

    private static void validatePaymentOwnership(Payment payment, Long requesterId, String requesterType) {
        if ("COMPANY".equalsIgnoreCase(requesterType)) {
            if (payment.getCompany() == null || !payment.getCompany().getCompanyId().equals(requesterId)) {
                throw new CustomException(ErrorCode.UNAUTHORIZED);
            }
        } else if ("STUDENT_ORG".equalsIgnoreCase(requesterType) || "STUDENTORG".equalsIgnoreCase(requesterType)) {
            if (payment.getStudentOrg() == null || !payment.getStudentOrg().getStudentOrgId().equals(requesterId)) {
                throw new CustomException(ErrorCode.UNAUTHORIZED);
            }
        } else {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
    }


    /**
     * 4. 결제 재시도 (FAILED 상태에서만 가능)
     */
    public PaymentDto.PaymentListResponse retryPayment(Long paymentId, Long requesterId, String requesterType) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        validatePaymentOwnership(payment, requesterId, requesterType);

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
     * 5. 세금계산서/영수증 발행 요청
     */
    public PaymentDto.InvoiceResponse requestInvoiceIssue(
            Long paymentId,
            Long requesterId,
            String requesterType,
            PaymentDto.InvoiceRequestCreateRequest request) {

        // Payment 존재 및 권한 확인
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
        validatePaymentOwnership(payment, requesterId, requesterType);

        // InvoiceRequest 생성
        return invoiceRequestService.requestInvoiceIssue(paymentId, request);
    }

    /**
     * Payment ID, 일반, user list로 조회
     */
    @Transactional(readOnly = true)
    public PaymentDto.InvoiceResponse getInvoiceRequestByPaymentId(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        // 권한 검증: 결제 소유자만 조회 가능
        return invoiceRequestService.getInvoiceRequestByPaymentId(paymentId);
    }

    @Transactional(readOnly = true)
    public PaymentDto.InvoiceResponse getInvoiceRequest(Long invoiceRequestId, Long requesterId, String requesterType) {
        PaymentDto.InvoiceResponse invoice = invoiceRequestService.getInvoiceRequest(invoiceRequestId);

        // 권한 검증: 요청자=해당 결제자?
        Payment payment = paymentRepository.findById(invoice.getPaymentId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
        validatePaymentOwnership(payment, requesterId, requesterType);

        return invoice;
    }

    @Transactional(readOnly = true)
    public List<PaymentDto.InvoiceResponse> getInvoiceRequests(Long requesterId, String requesterType) {
        return invoiceRequestService.getInvoiceRequestsByUserId(requesterId);
    }


    /**
     * DTO 변환 헬퍼 메서드
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
        builder.matchRequestId(matchRequest.getId())
                .matchRequestType(matchRequest.getCollaborationType() != null
                        ? matchRequest.getCollaborationType().toString() : "N/A");

        return builder.build();
    }
}