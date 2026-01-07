package com.uniConnect.payment.service;

import com.uniConnect.global.exception.CustomException;
import com.uniConnect.global.exception.ErrorCode;
import com.uniConnect.payment.dto.PaymentDto;
import com.uniConnect.payment.entity.InvoiceRequest;
import com.uniConnect.payment.enums.InvoiceStatus;
import com.uniConnect.payment.repository.InvoiceRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class InvoiceRequestService {

    private final InvoiceRequestRepository invoiceRequestRepository;

    /**
     * Payment 결제 성공 후 InvoiceRequest 자동 생성
     */
    public PaymentDto.InvoiceResponse createInvoiceRequestFromPayment(PaymentDto.InvoiceCreateRequest dto) {
        try {
            // 순수금액 계산
            Long netAmount = dto.getOriginalAmount() - dto.getDiscountAmount();

            // 전체 금액 유효성 검사
            Long calculatedTotal = netAmount + dto.getTaxAmount() + dto.getPartnershipFee() + dto.getAdditionalMarketing();
            if (!calculatedTotal.equals(dto.getTotalAmount())) {
                log.warn("⚠️ 금액 합계 불일치: 계산값={}, 요청값={}", calculatedTotal, dto.getTotalAmount());
                // 요청값을 그대로 사용 (오류 발생 안 함)
            }

            InvoiceRequest invoiceRequest = InvoiceRequest.builder()
                    .paymentId(dto.getPaymentId())
                    .paymentDateTime(dto.getPaymentDateTime())
                    .paymentMethod(dto.getPaymentMethod())
                    .customerName(dto.getCustomerName())
                    .email(dto.getEmail())
                    .phone(dto.getPhone())
                    .cardNumber(dto.getCardNumber())
                    .approvalNumber(dto.getApprovalNumber())
                    .originalAmount(dto.getOriginalAmount())
                    .discountAmount(dto.getDiscountAmount() != null ? dto.getDiscountAmount() : 0L)
                    .taxAmount(dto.getTaxAmount())
                    .partnershipFee(dto.getPartnershipFee())
                    .additionalMarketing(dto.getAdditionalMarketing() != null ? dto.getAdditionalMarketing() : 0L)
                    .totalAmount(dto.getTotalAmount())
                    .netAmount(netAmount)
                    .bizNumber(dto.getBizNumber())
                    .companyName(dto.getCompanyName())
                    .representativeName(dto.getRepresentativeName())
                    .bizType(dto.getBizType())
                    .bizItem(dto.getBizItem())
                    .status(InvoiceStatus.PENDING)
                    .userId(dto.getUserId())
                    .build();

            invoiceRequestRepository.save(invoiceRequest);

            log.info("✅ InvoiceRequest 자동 생성 성공: ID={}, PaymentId={}, 총액={}",
                    invoiceRequest.getInvoiceRequestId(), dto.getPaymentId(), dto.getTotalAmount());

            return convertToResponseDto(invoiceRequest);

        } catch (Exception e) {
            log.error("❌ InvoiceRequest 자동 생성 실패: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 사용자 발행 요청 (추가 정보 입력)
     */
    public PaymentDto.InvoiceResponse requestInvoiceIssue(
            Long paymentId,
            PaymentDto.InvoiceRequestCreateRequest request) {

        // 기존 InvoiceRequest 확인
        InvoiceRequest existingInvoice = invoiceRequestRepository.findByPaymentId(paymentId)
                .orElse(null);

        if (existingInvoice != null && existingInvoice.getStatus() == InvoiceStatus.PENDING) {
            log.warn("⚠️ 이미 발행 대기 중인 InvoiceRequest가 있습니다: PaymentId={}", paymentId);
            throw new CustomException(ErrorCode.ALREADY_EXISTS);
        }

        // 부가세 계산 (10%)
        Long taxAmount = (long) (request.getPartnershipFee() * 0.1);
        Long netAmount = request.getPartnershipFee() - request.getDiscountAmount();
        Long totalAmount = netAmount + taxAmount + request.getAdditionalMarketing();

        InvoiceRequest invoiceRequest = InvoiceRequest.builder()
                .paymentId(paymentId)
                .paymentDateTime(java.time.LocalDateTime.now())
                .paymentMethod(null)  // 기존 Payment에서 조회
                .customerName(request.getCompanyName())
                .email("")
                .phone("")
                .cardNumber("")
                .approvalNumber("")
                .originalAmount(request.getPartnershipFee())
                .discountAmount(request.getDiscountAmount())
                .taxAmount(taxAmount)
                .partnershipFee(request.getPartnershipFee())
                .additionalMarketing(request.getAdditionalMarketing())
                .totalAmount(totalAmount)
                .netAmount(netAmount)
                .bizNumber(request.getBizNumber())
                .companyName(request.getCompanyName())
                .representativeName(request.getRepresentativeName())
                .address(request.getAddress())
                .bizType(request.getBizType())
                .bizItem(request.getBizItem())
                .status(InvoiceStatus.PENDING)
                .userId(null)  // Controller에서 설정
                .build();

        invoiceRequestRepository.save(invoiceRequest);

        log.info("✅ InvoiceRequest 발행 요청 성공: ID={}, PaymentId={}",
                invoiceRequest.getInvoiceRequestId(), paymentId);

        return convertToResponseDto(invoiceRequest);
    }

    /**
     * InvoiceRequest 조회 (단건, paymentId, userId)
     */
    @Transactional(readOnly = true)
    public PaymentDto.InvoiceResponse getInvoiceRequest(Long invoiceRequestId) {
        InvoiceRequest invoiceRequest = invoiceRequestRepository.findById(invoiceRequestId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        return convertToResponseDto(invoiceRequest);
    }

    @Transactional(readOnly = true)
    public PaymentDto.InvoiceResponse getInvoiceRequestByPaymentId(Long paymentId) {
        InvoiceRequest invoiceRequest = invoiceRequestRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        return convertToResponseDto(invoiceRequest);
    }

    @Transactional(readOnly = true)
    public List<PaymentDto.InvoiceResponse> getInvoiceRequestsByUserId(Long userId) {
        return invoiceRequestRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * InvoiceRequest 상태 업데이트
     */
    public void updateInvoiceRequestStatus(Long invoiceRequestId, InvoiceStatus status) {
        InvoiceRequest invoiceRequest = invoiceRequestRepository.findById(invoiceRequestId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        invoiceRequest.setStatus(status);
        invoiceRequestRepository.save(invoiceRequest);

        log.info("✅ InvoiceRequest 상태 업데이트: ID={}, Status={}", invoiceRequestId, status);
    }

    /**
     * DTO 변환 헬퍼 메서드
     */
    private PaymentDto.InvoiceResponse convertToResponseDto(InvoiceRequest invoiceRequest) {
        return PaymentDto.InvoiceResponse.builder()
                .invoiceRequestId(invoiceRequest.getInvoiceRequestId())
                .paymentId(invoiceRequest.getPaymentId())
                .paymentDateTime(invoiceRequest.getPaymentDateTime())
                .paymentMethod(invoiceRequest.getPaymentMethod())
                .customerName(invoiceRequest.getCustomerName())
                .email(invoiceRequest.getEmail())
                .phone(invoiceRequest.getPhone())
                .cardNumber(invoiceRequest.getCardNumber())
                .approvalNumber(invoiceRequest.getApprovalNumber())
                .originalAmount(invoiceRequest.getOriginalAmount())
                .discountAmount(invoiceRequest.getDiscountAmount())
                .taxAmount(invoiceRequest.getTaxAmount())
                .partnershipFee(invoiceRequest.getPartnershipFee())
                .additionalMarketing(invoiceRequest.getAdditionalMarketing())
                .totalAmount(invoiceRequest.getTotalAmount())
                .netAmount(invoiceRequest.getNetAmount())
                .bizNumber(invoiceRequest.getBizNumber())
                .companyName(invoiceRequest.getCompanyName())
                .representativeName(invoiceRequest.getRepresentativeName())
                .address(invoiceRequest.getAddress())
                .bizType(invoiceRequest.getBizType())
                .bizItem(invoiceRequest.getBizItem())
                .status(invoiceRequest.getStatus())
                .createdAt(invoiceRequest.getCreatedAt())
                .build();
    }
}