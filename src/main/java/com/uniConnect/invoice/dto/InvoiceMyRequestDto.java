package com.uniConnect.invoice.dto.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniConnect.invoice.entity.InvoiceType;
import com.uniConnect.invoice.entity.InvoiceRequest;
import com.uniConnect.invoice.entity.InvoiceStatus;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class InvoiceMyRequestDto {
    private Long invoiceRequestId;
    private String eventName;
    private Long receivedAmount;
    private InvoiceStatus status;
    private String bizCertUrl;
    private String contractFileUrl;

    public static InvoiceMyRequestDto fromEntity(InvoiceRequest e) {
        return InvoiceMyRequestDto.builder()
                .invoiceRequestId(e.getInvoiceRequestId())
                .eventName(e.getEventName())
                .receivedAmount(e.getReceivedAmount())
                .status(e.getStatus())
                .bizCertUrl(e.getBizCertUrl())
                .contractFileUrl(e.getContractFileUrl())
                .build();
    }
}
