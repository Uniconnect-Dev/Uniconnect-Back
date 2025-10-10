package com.uniConnect.survey.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SamplingReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id")
    private Survey survey;

    private String reportTitle;
    private String content;
    private String fileUrl;

    @Enumerated(EnumType.STRING)
    private SamplingStatus status;

    private LocalDateTime submittedAt;

    @PrePersist
    public void prePersist() {
        this.status = SamplingStatus.SUBMITTED;
        this.submittedAt = LocalDateTime.now();
    }
}
