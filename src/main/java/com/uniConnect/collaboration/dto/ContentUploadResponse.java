package com.uniConnect.collaboration.dto;

import com.uniConnect.collaboration.entity.ContentUpload;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
public class ContentUploadResponse {
    private Long uploadId;
    private String imageUrl;
    private String caption;
    private String uploaderType;
    private LocalDateTime uploadedAt;

    public static ContentUploadResponse from(ContentUpload upload) {
        return ContentUploadResponse.builder()
                .uploadId(upload.getUploadId())
                .imageUrl(upload.getImageUrl())
                .caption(upload.getCaption())
                .uploaderType(upload.getUploaderType().name())
                .uploadedAt(upload.getUploadedAt())
                .build();
    }
}
