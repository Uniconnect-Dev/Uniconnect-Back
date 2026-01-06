package com.uniConnect.collaboration.entity;

import com.uniConnect.collaboration.entity.*;
import jakarta.persistence.*;
import lombok.*;
import com.uniConnect.collaboration.enums.*;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "receipt_confirmations")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ReceiptConfirmation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "receipt_id")
    private Long receiptId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collaboration_id", nullable = false)
    private Collaboration collaboration;

    @Column(columnDefinition = "text")
    private String receiptImageUrl;

    @Column(columnDefinition = "text")
    private String signatureImageBase64;

    @Column(name = "signature_timestamp")
    private Long signatureTimestamp;

    private String receiverName;
    private String location;

    private Integer receivedQuantity;
    private Boolean hasDefect;
    private LocalDate expirationDate;

    private LocalDateTime receivedAt;

    private LocalDateTime submittedAt;
    private LocalDateTime approvedAt;

    @Enumerated(EnumType.STRING)
    private ReceiptStatus status;

    public void approve() {
        this.status = ReceiptStatus.Approved;
        this.approvedAt = LocalDateTime.now();
    }
}
