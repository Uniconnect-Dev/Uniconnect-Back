package com.uniConnect.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "refund_requests")
public class RefundRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refund_id")
    private Long refundId;

    @Column(name = "reason", columnDefinition = "text")
    private String reason;

    @Column(name = "refund_amount")
    private Integer refundAmount;

    @Column(name = "bank_name", length = 50)
    private String bankName;

    @Column(name = "account_no_enc", length = 255)
    private String accountNoEnc;

    @Column(name = "holder_name", length = 50)
    private String holderName;

    @Column(name = "requester_name", length = 50)
    private String requesterName;

    @Column(name = "requester_email", length = 120)
    private String requesterEmail;

    @Column(name = "requester_phone", length = 20)
    private String requesterPhone;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;
}
