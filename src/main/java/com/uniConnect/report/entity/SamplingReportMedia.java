package com.uniConnect.report.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sampling_report_media")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SamplingReportMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "media_id")
    private Long mediaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id")
    private SamplingReport report;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type")
    private MediaType mediaType;  // IMAGE / VIDEO

    @Column(name = "media_url")
    private String mediaUrl;      // S3 URL

    @Column(name = "caption")
    private String caption;       // 선택 캡션

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public enum MediaType {
        IMAGE,
        VIDEO
    }
}
