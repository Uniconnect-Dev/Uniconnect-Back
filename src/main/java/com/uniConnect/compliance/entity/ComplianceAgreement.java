package com.uniConnect.compliance.entity;

import com.uniConnect.common.entity.BaseEntity;
import com.uniConnect.member.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "compliance_agreements")
public class ComplianceAgreement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "agreement_id")
    private Long agreementId;

    @Column(name = "request_id", nullable = false, length = 100)
    private String requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "process_info_accepted", nullable = false)
    private Boolean processInfoAccepted;

    @Column(name = "offplatform_penalty_accepted", nullable = false)
    private Boolean offplatformPenaltyAccepted;

    @Column(name = "terms_ack_accepted", nullable = false)
    private Boolean termsAckAccepted;

    @Column(name = "agreed_at", nullable = false)
    private LocalDateTime agreedAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;
}