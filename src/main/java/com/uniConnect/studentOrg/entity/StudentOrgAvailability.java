package com.uniConnect.studentOrg.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "student_org_availabilities")
public class StudentOrgAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "availability_id")
    private Long availabilityId;

    @Column(name = "event_name", length = 120)
    private String eventName;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "host_name", length = 120)
    private String hostName;

    @Column(name = "place", length = 120)
    private String place;

    @Column(name = "event_type", length = 60)
    private String eventType;

    // 규모 정보
    @Column(name = "exposure_count")
    private Integer exposureCount;                 // 노출 인원

    @Column(name = "recommended_sample_qty")
    private Integer recommendedSampleQty;          // 샘플 권장량

    // 참여자 특성
    @Column(name = "target_age_range", length = 60)
    private String targetAgeRange;

    @Column(name = "target_major", length = 120)
    private String targetMajor;

    @Column(name = "target_interest", length = 200)
    private String targetInterest;

    // 홍보/포인트/프로모션/효율
    @Column(name = "promotion_process", columnDefinition = "text")
    private String promotionProcess;

    @Column(name = "event_points", columnDefinition = "text")
    private String eventPoints;

    @Column(name = "promotion_plan", columnDefinition = "text")
    private String promotionPlan;

    @Column(name = "efficiency_metric", columnDefinition = "text")
    private String efficiencyMetric;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_org_id")
    private StudentOrg studentOrg;
}
