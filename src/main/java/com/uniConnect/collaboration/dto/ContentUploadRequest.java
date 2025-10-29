package com.uniConnect.collaboration.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ContentUploadRequest {
    private Long collaborationId;
    private String caption;
    private String uploaderType; // "Company" / "StudentOrg"
    private MultipartFile image; // 카드뉴스나 제품사진
}
