package com.uniConnect.collaboration.entity;

import com.uniConnect.campaign.entity.MatchingRequest;
import com.uniConnect.collaboration.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "collaboration_tasks")
public class CollaborationTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Long taskId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 30)
    private TaskType type; // DATE_FIX, PRODUCT_INFO, CONTENT_UPLOAD, RECEIPT, etc.

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private TaskStatus status; // NOT_STARTED, IN_PROGRESS, COMPLETED

    @Column(name = "deadline")
    private LocalDate deadline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collaboration_id")
    private Collaboration collaboration;

    @Column(name = "updated_by", length = 20)
    private String updatedBy; // "COMPANY" / "STUDENT_ORG"

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}