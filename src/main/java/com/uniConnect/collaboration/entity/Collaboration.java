package com.uniConnect.collaboration.entity;

import com.uniConnect.campaign.entity.MatchingRequest;
import jakarta.persistence.*;
import lombok.*;
import com.uniConnect.collaboration.enums.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "collaborations")
public class Collaboration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "collaboration_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matching_id")
    private MatchingRequest matching;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private CollaborationStatus status; // Ready, InProgress, WaitingReceipt, Completed

    @OneToMany(mappedBy = "collaboration", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductInfo> productInfos;

    @OneToMany(mappedBy = "collaboration", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContentUpload> contentUploads;

    @OneToMany(mappedBy = "collaboration", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CollaborationTask> tasks;

    @OneToMany(mappedBy = "collaboration", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReceiptConfirmation> receipts;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.status = CollaborationStatus.Ready;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
