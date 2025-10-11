package com.uniConnect.payment.entity;

import com.uniConnect.company.entity.Company;
import com.uniConnect.payment.enums.PaymentMethodType;
import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "payment_methods")
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "method_id")
    private Long methodId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20)
    private PaymentMethodType type;

    @Column(name = "card_number_enc", length = 255)
    private String cardNumberEnc;

    @Column(name = "expiry", length = 10)
    private String expiry;

    @Column(name = "cvc_enc", length = 50)
    private String cvcEnc;

    @Column(name = "bank_name", length = 50)
    private String bankName;

    @Column(name = "account_no_enc", length = 255)
    private String accountNoEnc;

    @Column(name = "holder_name", length = 50)
    private String holderName;

    @Column(name = "is_default")
    private Boolean isDefault;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;
}
