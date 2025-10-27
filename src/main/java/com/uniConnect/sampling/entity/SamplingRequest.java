package com.uniConnect.sampling.entity;

import com.uniConnect.member.entity.User;
import com.uniConnect.sampling.enums.SamplingRequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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
    private User user; // 요청 생성자 (기업 계정)

    @Column(nullable = false, length = 100)
    private String title; // 요청 제목

    @Column(length = 500)
    private String description; // 요청 설명

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SamplingRequestStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.status = SamplingRequestStatus.Draft;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
