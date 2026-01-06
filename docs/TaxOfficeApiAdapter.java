package com.uniConnect.payment.entity;

import com.uniConnect.global.exception.CustomException;
import com.uniConnect.global.exception.ErrorCode;
import com.uniConnect.invoice.entity.InvoiceStatus;
import com.uniConnect.member.entity.BusinessRegistration;
import com.uniConnect.payment.dto.PaymentDto;
import com.uniConnect.payment.entity.Invoice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaxOfficeApiAdapter {

    private final RestTemplate restTemplate;

    @Value("${tax-office.api.url:https://api.nts.go.kr}")
    private String taxOfficeApiUrl;

    @Value("${tax-office.api.key:}")
    private String taxOfficeApiKey;

    /**
     * 세금계산서 발행 요청
     */
    public String issueTaxInvoice(Invoice invoice, BusinessRegistration businessReg) {
        try {
            // 1) 요청 데이터 생성
            Map<String, Object> requestBody = buildTaxInvoiceRequest(invoice, businessReg);

            // 2) HTTP 요청 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + taxOfficeApiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // 3) 국세청 API 호출
            String endpoint = taxOfficeApiUrl + "/v1/tax-invoice/issue";
            Map<String, Object> response = restTemplate.postForObject(
                    endpoint,
                    request,
                    Map.class
            );

            // 4) 응답에서 확인 번호 추출
            if (response != null && response.containsKey("confirmationNumber")) {
                String confirmationNumber = (String) response.get("confirmationNumber");
                log.info("✅ 세금계산서 발행 성공: {} - {}", invoice.getInvoiceId(), confirmationNumber);
                return confirmationNumber;
            } else {
                throw new CustomException(ErrorCode.INVALID_INVOICE_TYPE, "국세청 API 응답에 확인번호가 없습니다");
            }

        } catch (RestClientException e) {
            log.error("❌ 국세청 API 호출 실패: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.RESTAPI_NOT_ALLOWED, "세금계산서 발행 중 외부 api 오류입니다.");
        }
    }

    /**
     * 현금영수증 발행 요청
     */
    public String issueCashReceipt(Invoice invoice, BusinessRegistration businessReg) {
        try {
            // 1) 요청 데이터 생성
            Map<String, Object> requestBody = buildCashReceiptRequest(invoice, businessReg);

            // 2) HTTP 요청 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + taxOfficeApiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // 3) 국세청 API 호출
            String endpoint = taxOfficeApiUrl + "/v1/cash-receipt/issue";
            Map<String, Object> response = restTemplate.postForObject(
                    endpoint,
                    request,
                    Map.class
            );

            // 4) 응답에서 확인 번호 추출
            if (response != null && response.containsKey("confirmationNumber")) {
                String confirmationNumber = (String) response.get("confirmationNumber");
                log.info("✅ 현금영수증 발행 성공: {} - {}", invoice.getInvoiceId(), confirmationNumber);
                return confirmationNumber;
            } else {
                throw new CustomException(ErrorCode.INVALID_INVOICE_TYPE, "국세청 API 응답에 확인번호가 없습니다");
            }

        } catch (RestClientException e) {
            log.error("❌ 국세청 API 호출 실패: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.RESTAPI_NOT_ALLOWED, "현금영수증 발행 중 오류가 발생했습니다");
        }
    }

    /**
     * 세금계산서 요청 데이터 생성
     */
    private Map<String, Object> buildTaxInvoiceRequest(Invoice invoice, BusinessRegistration businessReg) {
        Map<String, Object> request = new HashMap<>();

        // 발급자 정보 (판매자)
        request.put("issuerTaxId", businessReg.getRegistrationNo());
        request.put("issuerName", businessReg.getCompanyName());
        request.put("issuerAddress", businessReg.getAddress());

        // 수취인 정보 (구매자)
        request.put("buyerTaxId", invoice.getPayment().getCompany().getTaxNumber());
        request.put("buyerName", invoice.getPayment().getCompany().getName());

        // 거래 정보
        request.put("invoiceDate", LocalDateTime.now());
        request.put("description", "uniConnect 광고료");
        request.put("amount", invoice.getAmount());
        request.put("taxAmount", invoice.getTaxAmount());
        request.put("totalAmount", invoice.getAmount() + invoice.getTaxAmount());

        // 메타 정보
        request.put("invoiceNumber", UUID.randomUUID().toString());
        request.put("externalKey", "INVOICE-" + invoice.getInvoiceId());

        return request;
    }

    /**
     * 현금영수증 요청 데이터 생성
     */
    private Map<String, Object> buildCashReceiptRequest(Invoice invoice, BusinessRegistration businessReg) {
        Map<String, Object> request = new HashMap<>();

        // 발급자 정보
        request.put("issuerTaxId", businessReg.getBusinessRegistrationNumber());
        request.put("issuerName", businessReg.getCompanyName());

        // 거래 정보
        request.put("receiptDate", LocalDateTime.now());
        request.put("description", "uniConnect 광고료");
        request.put("amount", invoice.getAmount());
        request.put("taxAmount", invoice.getTaxAmount());
        request.put("totalAmount", invoice.getAmount() + invoice.getTaxAmount());

        // 메타 정보
        request.put("receiptNumber", UUID.randomUUID().toString());
        request.put("externalKey", "RECEIPT-" + invoice.getInvoiceId());

        return request;
    }
}}