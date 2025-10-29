package com.uniConnect.common.service.impl;

import com.uniConnect.common.service.FileStorageService;
import com.uniConnect.s3.S3FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 기존 S3FileService를 감싸는 어댑터 클래스
 * CollaborationDashboardService는 FileStorageService 타입으로 주입받고,
 * 실제 동작은 내부에서 S3FileService가 수행
 */
@Service
@RequiredArgsConstructor
public class S3FileStorageAdapter implements FileStorageService {

    private final S3FileService s3FileService;

    @Override
    public String uploadFile(String bucket, String key, MultipartFile file) {
        try {
            return s3FileService.upload(bucket, key, file);
        } catch (Exception e) {
            throw new RuntimeException("S3 업로드 실패", e);
        }
    }

    @Override
    public void deleteFile(String bucket, String key) {
        s3FileService.delete(bucket, key);
    }
}