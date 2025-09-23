package com.uniConnect.s3.dto;

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

public record UploadRequest(

        // 키를 클라이언트가 넘길 수 있게 하되, 화이트리스트 패턴 및 길이 제한
        @Size(max = 512, message = "key 길이는 512자를 넘을 수 없습니다.")
        // 허용: 영문/숫자/.-_/ 폴더 구조, 상대/절대 경로 금지
        @Pattern(regexp = "^[a-zA-Z0-9._\\-/]+$", message = "key에는 영문/숫자/.-_/만 허용됩니다.")
        String key
) { }