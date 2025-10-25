package com.uniConnect.collaboration.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ReceiptUploadRequest {
    private Long collaborationId;
    private String receiverName;
    private String location;
    private MultipartFile receiptImage; // 인수증 이미지
}
