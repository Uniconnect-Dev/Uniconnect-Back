package com.uniConnect.common.service.impl;

import com.uniConnect.common.service.FileStorageService;
import com.uniConnect.s3.S3FileService;
import lombok.*;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Primary;
import org.springframework.web.multipart.MultipartFile;

@Primary
@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private final S3FileService s3FileService;

    @Override
    public String uploadFile(String bucket, String key, MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return null;
            }

            s3FileService.upload(bucket, key, file);

            return String.format("https://%s.s3.amazonaws.com/%s", bucket, key);

        } catch (Exception e) {
            System.err.println("[FileStorageServiceImpl] S3 업로드 실패: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void deleteFile(String bucket, String key) {
        try {
            s3FileService.delete(bucket, key);
        } catch (Exception e) {
            System.err.println("[FileStorageServiceImpl] S3 삭제 실패: " + e.getMessage());
        }
    }
}
