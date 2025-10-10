package com.uniConnect.member.entity;

import com.uniConnect.member.enums.VerifiedStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "business_registrations")
public class BusinessRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "registration_id")
    private Long registrationId;

    // info
    @Column(name = "registration_no", length = 20)
    private String registrationNo;

    @Column(name = "company_name", length = 100)
    private String companyName;

    @Column(name = "representative_name", length = 50)
    private String representativeName;

    @Column(name = "open_date")
    private LocalDate openDate;

    @Column(name = "biz_type", length = 50)
    private String bizType;

    @Column(name = "biz_item", length = 50)
    private String bizItem;

    @Column(name = "certificate_url", columnDefinition = "text")
    private String certificateUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "verified_status", length = 20)
    private VerifiedStatus verifiedStatus;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    // relation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
