package com.uniConnect.common.service;

import org.springframework.web.multipart.MultipartFile;

//s3service만 사용
public interface FileStorageService {

    String uploadFile(String bucket, String key, MultipartFile file);

    void deleteFile(String bucket, String key);

    default String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "unnamed_" + System.currentTimeMillis();
        }

        String bucket = "uniconnect-bucket";
        String key = "uploads/" + System.currentTimeMillis() + "_" + originalFilename;

        try {
            return uploadFile(bucket, key, file);
        } catch (Exception e) {
            System.err.println("[FileStorageService] uploadImage failed: " + e.getMessage());
            return null;
        }
    }
}
