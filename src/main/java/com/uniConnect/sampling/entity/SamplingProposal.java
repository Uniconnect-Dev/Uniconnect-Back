package com.uniConnect.sampling.entity;

import com.uniConnect.member.entity.User;
import com.uniConnect.sampling.enums.IndustryType;
import com.uniConnect.sampling.entity.SamplingTargetSelection;
import com.uniConnect.sampling.enums.SamplingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sampling_proposal")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SamplingProposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long proposalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_user_id", nullable = false)
    private User creator; // 기업 계정

    // ===== 1페이지 =====
    private String productName;

    @Enumerated(EnumType.STRING)
    private IndustryType industry;

    @Column(columnDefinition = "TEXT")
    private String samplingPurpose;

    private LocalDate samplingStartDate;
    private LocalDate samplingEndDate;

    private Integer productCount;

    @Column(columnDefinition = "TEXT")
    private String detailRequest;

    private String proposalFileUrl;

    // ===== 2페이지: 타겟 조건 =====
    @Builder.Default
    @OneToMany(mappedBy = "samplingProposal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SamplingTargetSelection> selections = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private SamplingStatus status; // Draft / Submitted / Closed

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (status == null) status = SamplingStatus.Draft;
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}