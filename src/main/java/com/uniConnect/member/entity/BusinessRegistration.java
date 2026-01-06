package com.uniConnect.member.entity;

import com.uniConnect.company.entity.Company;
import com.uniConnect.member.enums.VerifiedStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "business_registrations")
//기업mem 정보, invoice 발행
public class BusinessRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "registration_id")
    private Long registrationId;

    // info
    @Column(name = "registration_no", length = 20)
    private String registrationNo; //사업자등록번호

    @Column(name = "company_name", length = 100)
    private String companyName;

    @Column(name = "representative_name", length = 50)
    private String representativeName; //대표자명

    @Column(name = "open_date")
    private LocalDate openDate; //개업연월일

    @Column(name = "biz_type", length = 50)
    private String bizType; //사업업태

    @Column(name = "biz_item", length = 50)
    private String bizItem; //종목

    @Column(name = "certificate_url", columnDefinition = "text")
    private String certificateUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "verified_status", length = 20)
    private VerifiedStatus verifiedStatus; //Pending, Verified, Rejected

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    // relation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;  // 추가: 회사 정보 추적
}
