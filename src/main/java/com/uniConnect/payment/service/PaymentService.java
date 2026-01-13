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
import com.uniConnect.shop.entity.CartItem;
import com.uniConnect.shop.entity.Product;
import com.uniConnect.shop.repository.CartItemRepository;
import com.uniConnect.shop.repository.CartRepository;
import com.uniConnect.shop.repository.ProductRepository;
import com.uniConnect.studentOrg.entity.StudentOrg;
import com.uniConnect.studentOrg.repository.StudentOrgRepository;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final ProductRepository productRepository;
    private final CompanyRepository companyRepository;
    private final CompanyContactRepository companyContactRepository;
    private final StudentOrgRepository studentOrgRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final DummyPgGatewayAdapter pgGatewayAdapter;
    private final PaymentNotificationServiceImpl notificationService;
    private final CollaborationMatchRequestRepository collaborationMatchRequestRepository;
    private final InvoiceRequestService invoiceRequestService;

    private boolean matchPaymentState(Payment payment, String paymentState) {
        if (paymentState == null) return true;

        return switch (paymentState) {
            case "PENDING" ->
                    payment.getStatus() == PaymentStatus.PENDING ||
                            payment.getStatus() == PaymentStatus.PROCESSING;

            case "COMPLETED" ->
                    payment.getStatus() == PaymentStatus.SUCCESS;

            case "FAILED" ->
                    payment.getStatus() == PaymentStatus.FAILED ||
                            payment.getStatus() == PaymentStatus.CANCELED;

            case "REFUNDED" ->
                    payment.getStatus() == PaymentStatus.REFUNDING ||
                            payment.getStatus() == PaymentStatus.REFUNDED;

            default -> true;
        };
    }

    /**
     * 1. 기업의 모든 결제 내역 조회
     */
    @Transactional(readOnly = true)
    public List<PaymentDto.PaymentListResponse> getPayments(
            Long companyId,
            String paymentState,
            String studentOrgName
    ) {
        // 회사 존재 여부 확인
        companyRepository.findById(companyId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        log.info("[결제 내역 조회] Company ID: {}", companyId);

        return paymentRepository
                .findByCompanyCompanyIdOrderByCreatedAtDesc(companyId)
                .stream()

                .filter(p -> matchPaymentState(p, paymentState))

                .filter(p -> {
                    if (studentOrgName == null || studentOrgName.isBlank()) return true;
                    StudentOrg org = p.getStudentOrg();
                    return org != null &&
                            org.getOrganizationName().contains(studentOrgName);
                })

                .map(this::convertToPaymentListResponse)
                .collect(Collectors.toList());
    }

    /**
     * 1-2. 학생단체별 결제 내역 조회
     */
    @Transactional(readOnly = true)
    public List<PaymentDto.PaymentListResponse> getPaymentsByStudentOrg(
            Long studentOrgId,
            String paymentState,
            String companyName
    ) {
        studentOrgRepository.findById(studentOrgId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        return paymentRepository.findByStudentOrgStudentOrgIdOrderByCreatedAtDesc(studentOrgId)
                .stream()

                .filter(p -> matchPaymentState(p, paymentState))

                .filter(p -> {
                    if (companyName == null || companyName.isBlank()) return true;
                    Company company = p.getCompany();
                    return company != null &&
                            company.getBrandName().contains(companyName);
                })
                .map(this::convertToPaymentListResponse)
                .collect(Collectors.toList());
    }

    /**
     * 2. 결제 진행 (CollabMatchReq 결제)
     */
    //초기: company, studentOrg 나눠관리
    public PaymentDto.PaymentListResponse createPayment(PaymentDto.PaymentCreateRequest request) {
        // 1) 조회
        CollaborationMatchRequest matchRequest= collaborationMatchRequestRepository
                .findById(request.getCollaborationMatchRequestId())
                .orElseThrow(()->new CustomException(ErrorCode.NOT_FOUND));

        // 2) 권한 확인
        validateMatchRequestPermission(matchRequest, request.getRequesterType());

        // 3) 결제 수단 조회, 권한 확인
        PaymentMethod method = paymentMethodRepository.findById(request.getPaymentMethodId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

//        validatePaymentMethodPermission(method, matchRequest);

        // 4) Payment 엔티티 생성
        // sender가 결제자이므로:
        // - STUDENT_ORG sender: studentOrg가 company에게 결제
        // - COMPANY sender: company가 studentOrg에게 결제
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
     * 기업이 requesterType: 기업이 matchSender
     */
    private void validateMatchRequestPermission(CollaborationMatchRequest matchRequest, String requesterType) {
        if ("COMPANY".equalsIgnoreCase(requesterType)) {
            if (!"COMPANY".equals(matchRequest.getSender().toString())) {
                throw new CustomException(ErrorCode.UNAUTHORIZED);
            }
        }
        else if ("STUDENT_ORG".equalsIgnoreCase(requesterType) || "STUDENTORG".equalsIgnoreCase(requesterType)) {
            if (!"STUDENT_ORG".equals(matchRequest.getSender().toString())) {
                throw new CustomException(ErrorCode.UNAUTHORIZED);
            }
        }
        else throw new CustomException(ErrorCode.UNAUTHORIZED);
    }

    // payment 검증: 송신자 정보가 collabMatchReq에 없어 불가
//    private void validatePaymentMethodPermission(PaymentMethod method, CollaborationMatchRequest matchRequest, String requesterType) {
//        Company company = matchRequest.getCompany();
//        StudentOrg studentOrg = matchRequest.getStudentOrg();
//
//        if (company != null && method.getCompany() != null) {
//            if (!method.getCompany().getCompanyId().equals(company.getCompanyId())) {
//                throw new CustomException(ErrorCode.UNAUTHORIZED);
//            }
//        } else if (studentOrg != null && method.getStudentOrg() != null) {
//            if (!method.getStudentOrg().getStudentOrgId().equals(studentOrg.getStudentOrgId())) {
//                throw new CustomException(ErrorCode.UNAUTHORIZED);
//            }
//        } else {
//            throw new CustomException(ErrorCode.UNAUTHORIZED);
//        }
//    }

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
     * Product 결제 진행 (StudentOrg 기반)
     */
    public PaymentDto.ProductPaymentResponse createProductPayment(
            Long studentOrgId,
            PaymentDto.ProductPaymentCreateRequest request) {

        // 1) StudentOrg 존재 확인
        StudentOrg studentOrg = studentOrgRepository.findById(studentOrgId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        // 2) Product 조회
        Product product = productRepository.findByIdWithCompany(request.getProductId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        // 3) 결제 수단 조회 및 권한 확인 (StudentOrg 결제 수단만 사용 가능)
        PaymentMethod method = paymentMethodRepository.findById(request.getPaymentMethodId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        if (method.getStudentOrg() == null || !method.getStudentOrg().getStudentOrgId().equals(studentOrgId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // 4) 총 결제 금액 계산
        Integer unitPrice = product.getPrice();
        Integer totalAmount = unitPrice * request.getQuantity();

        // 5) Payment 엔티티 생성 (StudentOrg 구매)
        Payment payment = Payment.builder()
                .studentOrg(studentOrg)
                .product(product)
                .amount(totalAmount)
                .method(method)
                .status(PaymentStatus.PROCESSING)
                .build();
        paymentRepository.save(payment);

        try {
            // 6) PG사에 결제 요청
            String transactionId = pgGatewayAdapter.processPayment(
                    payment.getPaymentId(),
                    totalAmount,
                    method
            );

            // 7) 결제 성공 업데이트
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setTransactionId(transactionId);
            payment.setCompletedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            log.info("[Product 결제 성공] PaymentId={}, StudentOrgId={}, ProductId={}, Quantity={}, Amount={}",
                    payment.getPaymentId(), studentOrgId, product.getProductId(), request.getQuantity(), totalAmount);

            // 8) 알림 발송
            notificationService.notifyPaymentSuccess(payment);

            return convertToProductPaymentResponse(payment, product, request.getQuantity());

        } catch (Exception e) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);

            log.error("[Product 결제 실패] PaymentId={}, StudentOrgId={}, Error={}",
                    payment.getPaymentId(), studentOrgId, e.getMessage(), e);
            notificationService.notifyPaymentFailed(payment);
            throw new CustomException(ErrorCode.PAYMENT_FAILED);
        }
    }

    /**
     * Product 결제 취소 (StudentOrg 기반)
     */
    public void cancelProductPayment(Long paymentId, Long studentOrgId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        // Product 결제인지 확인
        if (payment.getProduct() == null) {
            throw new CustomException(ErrorCode.INVALID_PAYMENT_STATUS);
        }

        // StudentOrg 소유권 확인
        if (payment.getStudentOrg() == null || !payment.getStudentOrg().getStudentOrgId().equals(studentOrgId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        if (payment.getStatus() != PaymentStatus.PROCESSING) {
            throw new CustomException(ErrorCode.INVALID_PAYMENT_STATUS);
        }

        payment.setStatus(PaymentStatus.CANCELED);
        payment.setCanceledAt(LocalDateTime.now());
        paymentRepository.save(payment);

        log.info("[Product 결제 취소] PaymentId={}, StudentOrgId={}, ProductId={}",
                payment.getPaymentId(), studentOrgId, payment.getProduct().getProductId());
        notificationService.notifyPaymentFailed(payment);
    }

    /**
     * Product 결제 재시도 (StudentOrg 기반)
     */
    public PaymentDto.ProductPaymentResponse retryProductPayment(Long paymentId, Long studentOrgId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        // Product 결제인지 확인
        if (payment.getProduct() == null) {
            throw new CustomException(ErrorCode.INVALID_PAYMENT_STATUS);
        }

        // StudentOrg 소유권 확인
        if (payment.getStudentOrg() == null || !payment.getStudentOrg().getStudentOrgId().equals(studentOrgId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        if (payment.getStatus() != PaymentStatus.FAILED) {
            throw new CustomException(ErrorCode.INVALID_PAYMENT_STATUS);
        }

        payment.setStatus(PaymentStatus.PROCESSING);
        paymentRepository.save(payment);

        log.info("[Product 결제 재시도] PaymentId={}, StudentOrgId={}", payment.getPaymentId(), studentOrgId);

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

            log.info("[Product 결제 재시도 성공] PaymentId={}, StudentOrgId={}",
                    payment.getPaymentId(), studentOrgId);
            notificationService.notifyPaymentSuccess(payment);

            Integer quantity = payment.getAmount() / payment.getProduct().getPrice();
            return convertToProductPaymentResponse(payment, payment.getProduct(), quantity);

        } catch (Exception e) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            log.error("[Product 결제 재시도 실패] PaymentId={}, Error={}", payment.getPaymentId(), e.getMessage(), e);
            throw new CustomException(ErrorCode.PAYMENT_FAILED);
        }
    }

    /**
     * DTO 변환: Product 결제 응답
     */
    private PaymentDto.ProductPaymentResponse convertToProductPaymentResponse(
            Payment payment, Product product, Integer quantity) {

        return PaymentDto.ProductPaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .productId(product.getProductId())
                .productName(product.getName())
                .unitPrice(product.getPrice())
                .quantity(quantity)
                .totalAmount(payment.getAmount())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .createdAt(payment.getCreatedAt())
                .completedAt(payment.getCompletedAt())
                .build();
    }

    /**
     * 장바구니 결제 진행 (다중 상품)
     */
    public PaymentDto.CartPaymentResponse createCartPayment(
            Long studentOrgId,
            PaymentDto.CartPaymentCreateRequest request) {

        // 1) StudentOrg 존재 확인
        StudentOrg studentOrg = studentOrgRepository.findById(studentOrgId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        // 2) 결제 수단 조회 및 권한 확인 (StudentOrg 결제 수단만 사용 가능)
        PaymentMethod method = paymentMethodRepository.findById(request.getPaymentMethodId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        if (method.getStudentOrg() == null || !method.getStudentOrg().getStudentOrgId().equals(studentOrgId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // 3) 장바구니 아이템 검증 및 금액 계산
        List<PaymentDto.CartPaymentCreateRequest.CartItemRequest> cartItems = request.getCartItems();
        if (cartItems == null || cartItems.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        int totalAmount = 0;
        List<PaymentDto.CartPaymentResponse.CartItemResponse> responseItems = new ArrayList<>();

        for (PaymentDto.CartPaymentCreateRequest.CartItemRequest item : cartItems) {
            Product product = productRepository.findByIdWithCompany(item.getProductId())
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

            Integer subtotal = product.getPrice() * item.getQuantity();
            totalAmount += subtotal;

            responseItems.add(PaymentDto.CartPaymentResponse.CartItemResponse.builder()
                    .productId(product.getProductId())
                    .productName(product.getName())
                    .unitPrice(product.getPrice())
                    .quantity(item.getQuantity())
                    .subtotal(subtotal)
                    .build());
        }

        // 4) Payment 엔티티 생성 (Cart 기반)
        Payment payment = Payment.builder()
                .studentOrg(studentOrg)
                .amount(totalAmount)
                .method(method)
                .status(PaymentStatus.PROCESSING)
                .build();
        paymentRepository.save(payment);

        try {
            // 5) PG사에 결제 요청
            String transactionId = pgGatewayAdapter.processPayment(
                    payment.getPaymentId(),
                    totalAmount,
                    method
            );

            // 6) 결제 성공 업데이트
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setTransactionId(transactionId);
            payment.setCompletedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            log.info("[Cart 결제 성공] PaymentId={}, StudentOrgId={}, 상품수={}, 총금액={}",
                    payment.getPaymentId(), studentOrgId, cartItems.size(), totalAmount);

            // 7) 알림 발송
            notificationService.notifyPaymentSuccess(payment);

            // 8) 결제 후 장바구니 초기화 (선택사항)
            clearStudentOrgCart(studentOrgId);

            return PaymentDto.CartPaymentResponse.builder()
                    .paymentId(payment.getPaymentId())
                    .totalAmount(totalAmount)
                    .status(payment.getStatus())
                    .transactionId(transactionId)
                    .createdAt(payment.getCreatedAt())
                    .completedAt(payment.getCompletedAt())
                    .items(responseItems)
                    .build();

        } catch (Exception e) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);

            log.error("[Cart 결제 실패] PaymentId={}, StudentOrgId={}, Error={}",
                    payment.getPaymentId(), studentOrgId, e.getMessage(), e);
            notificationService.notifyPaymentFailed(payment);
            throw new CustomException(ErrorCode.PAYMENT_FAILED);
        }
    }

    /**
     * 장바구니 초기화 (결제 후)
     */
    private void clearStudentOrgCart(Long studentOrgId) {
        try {
            cartRepository.findByStudentOrgStudentOrgId(studentOrgId)
                    .ifPresent(cart -> {
                        cart.getItems().clear();
                        cartRepository.save(cart);
                        log.info("[장바구니 초기화] StudentOrgId={}", studentOrgId);
                    });
        } catch (Exception e) {
            log.warn("[장바구니 초기화 실패] StudentOrgId={}, Error={}", studentOrgId, e.getMessage());
            // 장바구니 초기화 실패는 결제 롤백하지 않음
        }
    }

    /**
     * 결제 소유권 검증 (Payment 객체 기반)
     */
    private void validatePaymentOwnership(Payment payment, Long requesterId) {
        if (payment.getCompany() != null) {
            if (!payment.getCompany().getCompanyId().equals(requesterId)) {
                throw new CustomException(ErrorCode.UNAUTHORIZED);
            }
        } else if (payment.getStudentOrg() != null) {
            if (!payment.getStudentOrg().getStudentOrgId().equals(requesterId)) {
                throw new CustomException(ErrorCode.UNAUTHORIZED);
            }
        } else {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
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