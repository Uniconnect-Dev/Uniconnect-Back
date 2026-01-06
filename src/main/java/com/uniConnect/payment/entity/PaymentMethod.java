package com.uniConnect.payment.entity;

import com.uniConnect.company.entity.Company;
import com.uniConnect.payment.enums.PaymentMethodType;
import com.uniConnect.studentOrg.entity.StudentOrg;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

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
    private PaymentMethodType type; //항상 card

    @Column(name = "card_number_enc", length = 255)
    private String cardNumberEnc;

    @Column(name = "expiry", length = 10)
    private String expiry;

    @Column(name = "cvc_enc", length = 50)
    private String cvcEnc;

    @Column(name = "bank_name", length = 50)
    private String bankName;

    @Column(name = "account_no_enc", length = 255)
    private String accountNoEnc; //암호화x 계좌번호

    @Column(name = "holder_name", length = 50)
    private String holderName;

//    // 간편결제
//    @Column(length = 100)
//    private String pgProvider;  // kakaopay, naverpay, tosspay 등
//
//    @Column(length = 255)
//    private String pgMerchantKey;  // PG사 merchant key

//    // 상태
//    @Column(nullable = false)
//    private Boolean isDefault = false;  // 기본 결제 수단 여부: 미구현으로 제외

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    //추가
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_org_id")
    private StudentOrg studentOrg;

    @OneToMany(mappedBy = "method")
    private List<Payment> payments; //새로 추가
}
