package com.uniConnect.collaboration.dto;

import com.uniConnect.collaboration.entity.ReceiptConfirmation;
import com.uniConnect.collaboration.enums.ReceiptStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
public class ReceiptResponse {
    private Long receiptId;
    private String receiverName;
    private String location;
    private String receiptImageUrl;
    private ReceiptStatus status;
    private LocalDateTime submittedAt;
    private LocalDateTime approvedAt;

    public static ReceiptResponse from(ReceiptConfirmation receipt) {
        return ReceiptResponse.builder()
                .receiptId(receipt.getReceiptId())
                .receiverName(receipt.getReceiverName())
                .location(receipt.getLocation())
                .receiptImageUrl(receipt.getReceiptImageUrl())
                .status(receipt.getStatus())
                .submittedAt(receipt.getSubmittedAt())
                .approvedAt(receipt.getApprovedAt())
                .build();
    }
}
