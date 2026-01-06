package com.uniConnect.collaboration.entity;

import com.uniConnect.collaboration.entity.*;
import com.uniConnect.collaboration.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "content_uploads")
public class ContentUpload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "upload_id")
    private Long uploadId;

    @Column(name = "image_url", columnDefinition = "text")
    private String imageUrl;

    @Column(name = "caption", columnDefinition = "text")
    private String caption;

    @Enumerated(EnumType.STRING)
    @Column(name = "uploader_type", length = 20)
    private UploaderType uploaderType; // COMPANY, STUDENT_ORG

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collaboration_id")
    private Collaboration collaboration;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @PrePersist
    public void prePersist() {
        this.uploadedAt = LocalDateTime.now();
    }
}