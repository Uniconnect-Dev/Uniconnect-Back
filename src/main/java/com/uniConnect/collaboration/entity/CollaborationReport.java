package com.uniConnect.collaboration.entity;

import com.uniConnect.collaboration.enums.ReportStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "collaboration_reports")
public class CollaborationReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collaboration_id")
    private Collaboration collaboration;

    @Column(name = "report_title", length = 200)
    private String reportTitle;

    @Column(name = "file_url", columnDefinition = "text")
    private String fileUrl;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private ReportStatus status; // Submitted, WaitingApproval, Approved

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @PrePersist
    public void prePersist() {
        this.uploadedAt = LocalDateTime.now();
        this.status = ReportStatus.Submitted;
    }

    @PreUpdate
    public void preUpdate() {
        this.uploadedAt = LocalDateTime.now();
    }
}
