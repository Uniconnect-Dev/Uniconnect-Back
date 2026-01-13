package com.uniConnect.matching.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "company_matching_statistics")
public class CompanyMatchingStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "total_matchings")
    private Integer totalMatchings = 0;

    @Column(name = "completed_matchings")
    private Integer completedMatchings = 0;

    @Column(name = "pending_matchings")
    private Integer pendingMatchings = 0;

    @Column(name = "failed_matchings")
    private Integer failedMatchings = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void incrementTotal() {
        this.totalMatchings++;
    }

    public void updateStatus(String oldStatus, String newStatus) {
        // 이전 상태 감소
        if ("Approved".equals(oldStatus)) {
            this.completedMatchings = Math.max(0, this.completedMatchings - 1);
        } else if ("Requested".equals(oldStatus)) {
            this.pendingMatchings = Math.max(0, this.pendingMatchings - 1);
        } else if ("Rejected".equals(oldStatus)) {
            this.failedMatchings = Math.max(0, this.failedMatchings - 1);
        }

        // 새로운 상태 증가
        if ("Approved".equals(newStatus)) {
            this.completedMatchings++;
        } else if ("Requested".equals(newStatus)) {
            this.pendingMatchings++;
        } else if ("Rejected".equals(newStatus)) {
            this.failedMatchings++;
        }
    }
}