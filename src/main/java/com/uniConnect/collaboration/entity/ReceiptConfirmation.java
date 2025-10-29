package com.uniConnect.collaboration.entity;

import com.uniConnect.campaign.entity.MatchingRequest;
import jakarta.persistence.*;
import lombok.*;
import com.uniConnect.collaboration.enums.*;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "receipt_confirmations")
public class ReceiptConfirmation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "receipt_id")
    private Long receiptId;

    private String receiverName;
    private String location;

    @Column(name = "receipt_image_url", columnDefinition = "text")
    private String receiptImageUrl;

    private LocalDateTime submittedAt;
    private LocalDateTime approvedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private ReceiptStatus status; // PENDING_UPLOAD, WAITING_APPROVAL, APPROVED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collaboration_id")
    private Collaboration collaboration;
}
