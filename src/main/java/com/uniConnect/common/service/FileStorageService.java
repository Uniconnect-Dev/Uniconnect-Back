package com.uniConnect.common.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String uploadImage(MultipartFile file);
}
