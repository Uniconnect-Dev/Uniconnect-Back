package com.uniConnect.invoice.dto.response;

import com.uniConnect.invoice.entity.InvoiceRequest;
import com.uniConnect.invoice.entity.InvoiceStatus;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class InvoiceRequestQueryDto {

    private Long invoiceRequestId;
    private String eventName;
    private Long receivedAmount;

    private String companyName;
    private String bizNumber;

    private InvoiceStatus status;

    public static InvoiceRequestQueryDto fromEntity(InvoiceRequest entity) {
        return InvoiceRequestQueryDto.builder()
                .invoiceRequestId(entity.getInvoiceRequestId())
                .eventName(entity.getEventName())
                .receivedAmount(entity.getReceivedAmount())
                .companyName(entity.getCompanyName())
                .bizNumber(entity.getBizNumber())
                .status(entity.getStatus())
                .build();
    }
}
