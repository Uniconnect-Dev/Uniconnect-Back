package com.uniConnect.admin.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "admin_metrics")
public class AdminMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "metric_id")
    private Long metricId;

    @Column(name = "total_campaigns")
    private Integer totalCampaigns;

    @Column(name = "total_samples")
    private Integer totalSamples;

    @Column(name = "total_exposure")
    private Integer totalExposure;

    @Column(name = "roi_case", length = 100)
    private String roiCase;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
