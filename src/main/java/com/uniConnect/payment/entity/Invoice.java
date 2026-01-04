package com.uniConnect.payment.entity;

import com.uniConnect.company.entity.Company;
import com.uniConnect.invoice.entity.InvoiceStatus;
import com.uniConnect.member.entity.BusinessRegistration;
import com.uniConnect.payment.enums.InvoiceIssueType;
import com.uniConnect.sampling.entity.SamplingRequest;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "invoices")
public class Invoice{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_id")
    private Long invoiceId;

    @Column(name = "registration_no", length = 20)
    private String registrationNo;  // 세금계산서 번호

//    private Integer amount;  // 금액
//    private Integer taxAmount;  // 세액
//    private Integer totalAmount;  // 합계

//    @Enumerated(EnumType.STRING)
//    private InvoiceStatus status;  // ISSUED, CANCELLED, PENDING
//
//    private LocalDateTime cancelledAt;  // 취소 일시

//    // 국세청 API 응답
//    @Column(length = 100)
//    private String taxOfficeConfirmationNumber;  // 국세청 확인 번호
//
//    @Column(columnDefinition = "text")
//    private String notes;  // 적요/비고

    // info
    @Column(name = "company_name", length = 100)
    private String companyName;

    @Column(name = "representative_name", length = 60)
    private String representativeName;

    @Column(name = "address", length = 200)
    private String address;

    @Column(name = "biz_type", length = 50)
    private String bizType;

    @Column(name = "biz_item", length = 50)
    private String bizItem;

    @Column(name = "email", length = 120)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "issue_type", length = 20)
    private InvoiceIssueType issueType; //tax, cash

    @Column(name = "issued_pdf_url", columnDefinition = "text")
    private String issuedPdfUrl;

    private LocalDateTime issuedAt;  // 발행 일시

    // relation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_registration_id")
    private BusinessRegistration businessRegistration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sampling_request_id")
    private SamplingRequest samplingRequest;
}
