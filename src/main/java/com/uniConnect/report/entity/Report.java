package com.uniConnect.report.entity;

import com.uniConnect.campaign.entity.Campaign;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @Column(name = "pdf_url", columnDefinition = "text")
    private String pdfUrl;

    // PostgreSQL 가정. MySQL이면 "json"으로 변경
    @Column(name = "kpi_json", columnDefinition = "jsonb")
    private String kpiJson;

    @Column(name = "qualitative_summary", columnDefinition = "text")
    private String qualitativeSummary;

    @Column(name = "financial_summary", columnDefinition = "text")
    private String financialSummary;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;
}
