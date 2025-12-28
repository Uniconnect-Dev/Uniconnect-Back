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
@Table(name = "sampling_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SamplingRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sampling_request_id")
    private Long samplingRequestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 요청 생성자 (학생 단체 계정)

    // Step1: 단체 기본 정보
    @Column(length = 100)
    private String schoolName; // 학교명

    @Column(length = 100)
    private String orgName; // 단체명

    @Column(length = 50)
    private String contactName; // 담당자명

    @Column(length = 20)
    private String phone; // 연락처

    @Column(length = 120)
    private String email; // 이메일

    // Step3: 행사 정보
    @Column(length = 150)
    private String eventTitle; // 행사명

    @Column(length = 500)
    private String eventDescription; // 행사 설명

    private Integer requestedQuantity; // 요청 수량

    private LocalDate eventStartDate; // 행사 시작일
    private LocalDate eventEndDate;   // 행사 종료일

    // Step4: 산업군 + 제안서
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private IndustryType industry; // 산업군

    private String proposalFileUrl; // 제안서 S3 URL

    @Column(length = 250)
    private String detailRequest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SamplingStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(length = 50)
    private String companyIndustry;   // 기업 산업군

    @Column(columnDefinition = "TEXT")
    private String samplingPurpose;   // 샘플링 목적

    private LocalDate samplingStartDate; // 샘플링 시작일
    private LocalDate samplingEndDate;   // 샘플링 종료일

    @Column(length = 100)
    private String productName;  // 제품/서비스명

    private Integer productCount; // 제품 개수

    @Builder.Default
    @OneToMany(mappedBy = "samplingRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SamplingTargetSelection> selections = new ArrayList<>();

    public void addSelection(SamplingTargetSelection selection) {
        this.selections.add(selection);
        selection.setSamplingRequest(this);
    }

    @Builder.Default
    @OneToMany(mappedBy = "samplingRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SamplingMatchedOrg> matchedOrgs = new ArrayList<>();

    public void addMatchedOrg(SamplingMatchedOrg matched) {
        matched.setSamplingRequest(this);
        this.matchedOrgs.add(matched);
    }

    @Builder.Default
    @OneToMany(mappedBy = "samplingRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SamplingSelectedCompany> selectedCompanies = new ArrayList<>();

    public void addSelectedCompany(SamplingSelectedCompany s) {
        s.setSamplingRequest(this);
        this.selectedCompanies.add(s);
    }

    @PrePersist
    public void prePersist() {
        if (this.status == null) this.status = SamplingStatus.Draft;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
