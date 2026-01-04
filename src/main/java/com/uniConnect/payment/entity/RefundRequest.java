package com.uniConnect.payment.entity;

import com.uniConnect.company.entity.Company;
import com.uniConnect.payment.enums.RefundStatus;
import com.uniConnect.sampling.entity.SamplingRequest;
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

    //환불자의 bank
    @Column(name = "bank_name", length = 50)
    private String bankName;

    @Column(name = "account_no_enc", length = 255)
    private String accountNoEnc;

    @Column(name = "holder_name", length = 50)
    private String holderName;

    //환불 요청자
    @Column(name = "requester_name", length = 50)
    private String requesterName;

    @Column(name = "requester_email", length = 120)
    private String requesterEmail;

    @Column(name = "requester_phone", length = 20)
    private String requesterPhone;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;  // 환불 시작 일시

//    // 상태 관리(새로 추가)
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private RefundStatus refundStatus;  // PENDING, PROCESSING, COMPLETED, REJECTED
//
//    @Column(columnDefinition = "text")
//    private String refundRejectionReason;  // 거절 사유 (거절 시)
//
//    private LocalDateTime completedAt;  // 환불 완료 일시
//
//    @Column(length = 100)
//    private String refundTransactionId;  // 환불 거래 ID


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "company_id", nullable = false)
//    private Company company;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "sampling_request_id")
//    private SamplingRequest samplingRequest;
}
