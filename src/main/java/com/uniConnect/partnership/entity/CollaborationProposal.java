package com.uniConnect.partnership.entity;

import com.uniConnect.company.entity.Company;
import com.uniConnect.company.entity.Industry;
import com.uniConnect.partnership.enums.ProposalStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "collaboration_proposals")
//기업 req->학생
public class CollaborationProposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "proposal_id")
    private Long proposalId;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "contact_name", length = 60)
    private String contactName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 120)
    private String email;

    @Column(name = "content", columnDefinition = "text")
    private String content;

    @Column(name = "proposal_url", columnDefinition = "text")
    private String proposalUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private ProposalStatus status;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "industry_id")
    private Industry industry;
}
