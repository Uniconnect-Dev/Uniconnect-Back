package com.uniConnect.invoice.dto.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniConnect.invoice.entity.InvoiceType;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceRequestCreateDto {

    private String eventName;
    private Long receivedAmount;

    // 파일은 JSON 안에 포함되지 않으므로 Controller에서 set 해줌
    private transient MultipartFile bizCertFile;
    private transient MultipartFile contractFile;

    // 기업 사업자 정보
    private String bizNumber;
    private String companyName;
    private String ceoName;
    private String address;
    private String bizType;
    private String contactEmail;

    private InvoiceType invoiceType;

    private String companyContactPhone;

    public static InvoiceRequestCreateDto fromJson(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, InvoiceRequestCreateDto.class);
        } catch (Exception e) {
            throw new RuntimeException("JSON 파싱 오류: " + e.getMessage());
        }
    }
}
