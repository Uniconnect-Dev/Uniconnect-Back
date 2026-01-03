package com.uniConnect.collaboration.entity;

import com.uniConnect.matching.entity.CollaborationMatchRequest;
import jakarta.persistence.*;
import lombok.*;
import com.uniConnect.collaboration.enums.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;


@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "collaborations")
public class Collaboration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "collaboration_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_request_id")
    private CollaborationMatchRequest matchRequest;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "status", length = 20)
    private CollaborationStatus status;

    @Column(name = "contract_url", columnDefinition = "text")
    private String contractUrl; // 전자계약 문서 URL

    @Column(name = "student_signed_at")
    private LocalDateTime studentSignedAt;

    @Column(name = "admin_contract_approved_at")
    private LocalDateTime adminContractApprovedAt;

    @OneToMany(mappedBy = "collaboration", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductInfo> productInfos;

    @OneToMany(mappedBy = "collaboration", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContentUpload> contentUploads;

    @OneToMany(mappedBy = "collaboration", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CollaborationTask> tasks;

    @OneToMany(mappedBy = "collaboration", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReceiptConfirmation> receipts;

    @OneToMany(mappedBy = "collaboration", orphanRemoval = true)
    private List<CollaborationReport> reports;



    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.status == null) {
            this.status = CollaborationStatus.ContractSent;
        }
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {

        this.updatedAt = LocalDateTime.now();
    }
}
