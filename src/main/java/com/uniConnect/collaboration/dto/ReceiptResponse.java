package com.uniConnect.collaboration.dto;

import com.uniConnect.collaboration.entity.ReceiptConfirmation;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiptResponse {

    private Long receiptId;

    private String receiptImageUrl;
    private String receiverName;
    private String location;

    private Integer receivedQuantity;
    private Boolean hasDefect;
    private LocalDate expirationDate;
    private LocalDateTime receivedAt;


    private LocalDateTime submittedAt;
    private LocalDateTime approvedAt;
    private String status;

    public static ReceiptResponse from(ReceiptConfirmation r) {
        return ReceiptResponse.builder()
                .receiptId(r.getReceiptId())
                .receiptImageUrl(r.getReceiptImageUrl())
                .receiverName(r.getReceiverName())
                .location(r.getLocation())
                .receivedQuantity(r.getReceivedQuantity())
                .hasDefect(r.getHasDefect())
                .expirationDate(r.getExpirationDate())
                .receivedAt(r.getReceivedAt())
                .submittedAt(r.getSubmittedAt())
                .approvedAt(r.getApprovedAt())
                .status(r.getStatus().name())
                .build();
    }
}