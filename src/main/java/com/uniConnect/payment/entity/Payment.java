package com.uniConnect.payment.entity;

import com.uniConnect.campaign.entity.Campaign;
import com.uniConnect.company.entity.Company;
import com.uniConnect.payment.enums.PaymentStatus;
import com.uniConnect.sampling.entity.SamplingRequest;
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

//    private String transactionId; //결제 gateway
//    private String receiptUrl;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "method_id")
    private PaymentMethod method;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "sampling_request_id")
//    private SamplingRequest samplingRequest;  // 샘플링별 결제
//
//
//    @OneToMany(mappedBy= "payment")
//    private List<Invoice> invoices;
//
//    @OneToMany(mappedBy = "payment")
//    private List<RefundRequest> refunds;
}
