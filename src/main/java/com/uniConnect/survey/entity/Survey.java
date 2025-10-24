package com.uniConnect.survey.entity;

import com.uniConnect.studentOrg.entity.StudentOrg;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Survey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long surveyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_org_id")
    private StudentOrg studentOrg;

    private String title;
    private String description;
    private String externalLink;

    @Enumerated(EnumType.STRING)
    private SurveyStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.status = SurveyStatus.Pending;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
