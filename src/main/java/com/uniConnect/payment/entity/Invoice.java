package com.uniConnect.payment.entity;

import com.uniConnect.payment.enums.InvoiceIssueType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_id")
    private Long invoiceId;

    // info
    @Column(name = "registration_no", length = 20)
    private String registrationNo;

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
    private InvoiceIssueType issueType;

    @Column(name = "issued_pdf_url", columnDefinition = "text")
    private String issuedPdfUrl;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    // relation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;
}
