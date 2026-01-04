package com.uniConnect.payment.service;

import com.uniConnect.global.exception.CustomException;
import com.uniConnect.global.exception.ErrorCode;
import com.uniConnect.member.entity.BusinessRegistration;
import com.uniConnect.member.repository.BusinessRegistrationRepository;
import com.uniConnect.payment.dto.*;
import com.uniConnect.payment.entity.Invoice;
import com.uniConnect.payment.entity.Payment;
import com.uniConnect.payment.repository.*;
import com.uniConnect.s3.S3FileService;
import lombok.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final BusinessRegistrationRepository businessRegistrationRepository;
    private final PaymentRepository paymentRepository;
    private final TaxOfficeApiAdapter taxOfficeApiAdapter;  // 국세청 API
    private final PdfGeneratorService pdfGeneratorService;
    private final S3FileService s3FileService;
    private final EmailService emailService;

    /**
     * 4. 세금계산서/영수증 발행
     */
    public PaymentDto.InvoiceResponse createInvoice(Long companyId, PaymentDto.InvoiceCreateRequest request) {
        // 1) 결제 정보 조회
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        // 2) 사업자 정보 조회
        BusinessRegistration businessReg = businessRegistrationRepository.findById(request.getBusinessRegistrationId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        // 3) 사업자 검증 확인
        if (!businessReg.getIsVerified()) {
            throw new CustomException(ErrorCode.BUSINESS_NOT_VERIFIED);
        }

        // 4) Invoice 엔티티 생성
        Invoice invoice = Invoice.builder()
                .invoiceType(request.getInvoiceType())
                .payment(payment)
                .company(payment.getCompany())
                .businessRegistration(businessReg)
                .samplingRequest(payment.getSamplingRequest())
                .amount(request.getAmount())
                .taxAmount(request.getTaxAmount())
                .status(InvoiceStatus.PENDING)
                .build();

        invoiceRepository.save(invoice);

        try {
            // 5) 국세청 API로 발행 요청
            if (request.getInvoiceType() == InvoiceType.TAX_INVOICE) {
                String confirmationNumber = taxOfficeApiAdapter.issueTaxInvoice(invoice, businessReg);
                invoice.setTaxOfficeConfirmationNumber(confirmationNumber);
                invoice.setStatus(InvoiceStatus.ISSUED);
            } else if (request.getInvoiceType() == InvoiceType.CASH_RECEIPT) {
                String confirmationNumber = taxOfficeApiAdapter.issueCashReceipt(invoice, businessReg);
                invoice.setTaxOfficeConfirmationNumber(confirmationNumber);
                invoice.setStatus(InvoiceStatus.ISSUED);
            }

            // 6) PDF 생성
            ByteArrayInputStream pdfStream = pdfGeneratorService.generateInvoicePdf(invoice);

            // 7) S3에 업로드
            String pdfUrl = s3FileService.uploadFile(
                    "invoices/" + invoice.getInvoiceId() + ".pdf",
                    pdfStream
            );
            invoice.setInvoiceUrl(pdfUrl);
            invoice.setIssuedAt(LocalDateTime.now());

            invoiceRepository.save(invoice);

            // 8) 담당자에게 이메일 발송
            emailService.sendInvoiceEmail(businessReg.getManagerEmail(), invoice, pdfUrl);

            return convertToInvoiceResponse(invoice);

        } catch (TaxOfficeException e) {
            invoice.setStatus(InvoiceStatus.FAILED);
            invoiceRepository.save(invoice);
            throw new CustomException(ErrorCode.INVOICE_CREATION_FAILED);
        }
    }

    /**
     * 세금계산서 다운로드
     */
    public ResponseEntity<Resource> downloadInvoicePdf(Long invoiceId, Long companyId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        // 권한 확인
        if (!invoice.getCompany().getCompanyId().equals(companyId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // S3에서 파일 다운로드
        InputStream fileStream = s3FileService.downloadFile(invoice.getInvoiceUrl());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=" + invoice.getInvoiceNumber() + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(fileStream));
    }

    // ... 추가 메서드들
}