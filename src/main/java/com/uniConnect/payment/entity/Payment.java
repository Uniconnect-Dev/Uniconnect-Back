package com.uniConnect.payment.entity;

import com.uniConnect.campaign.entity.Campaign;
import com.uniConnect.company.entity.Company;
import com.uniConnect.matching.entity.CollaborationMatchRequest;
import com.uniConnect.payment.enums.PaymentStatus;
import com.uniConnect.sampling.entity.SamplingRequest;
import com.uniConnect.shop.entity.Product;
import com.uniConnect.studentOrg.entity.StudentOrg;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @Column(name = "amount")
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private PaymentStatus status;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 결제 완료 시간
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // 결제 취소 시간
    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    // relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    //추가
    @ManyToOne(fetch = FetchType.LAZY) //entity 조회시 연관entity 바로조회x
    @JoinColumn(name = "student_org_id")
    private StudentOrg studentOrg;

    //추가
    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name="product_id")
    private Product product;

    //추가
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "collaboration_match_request_id")
    private CollaborationMatchRequest collaborationMatchRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "method_id")
    private PaymentMethod method;

    // PG사 거래 ID (PG Gateway에서 발급)
    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    // 영수증 URL
    @Column(name = "receipt_url", columnDefinition = "text")
    private String receiptUrl;

//    @OneToMany(mappedBy = "payment")
//    private List<RefundRequest> refunds;
}
