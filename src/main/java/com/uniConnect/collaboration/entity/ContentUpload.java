package com.uniConnect.collaboration.entity;

import com.uniConnect.campaign.entity.MatchingRequest;
import com.uniConnect.collaboration.enums.UploaderType;
import jakarta.persistence.*;
import lombok.*;

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
    private UploaderType uploaderType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matching_id")
    private MatchingRequest matching;
}
