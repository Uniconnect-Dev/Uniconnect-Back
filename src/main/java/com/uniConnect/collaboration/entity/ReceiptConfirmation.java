package com.uniConnect.collaboration.entity;

import com.uniConnect.campaign.entity.MatchingRequest;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "receipt_confirmations")
public class ReceiptConfirmation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "receipt_id")
    private Long receiptId;

    @Column(name = "receiver_name", length = 60)
    private String receiverName;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "receipt_image_url", columnDefinition = "text")
    private String receiptImageUrl;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matching_id")
    private MatchingRequest matching;
}
