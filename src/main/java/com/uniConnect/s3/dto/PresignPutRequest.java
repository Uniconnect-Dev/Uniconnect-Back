package com.uniConnect.s3.dto;

import jakarta.validation.constraints.*;

public record PresignPutRequest(
        @Size(max = 512)
        @Pattern(regexp = "^[a-zA-Z0-9._\\-/]+$")
        String key,

        @Size(max = 100)
        String contentType,

        @Positive @Max(3600) // 기본 상한
        long expiresInSec
) { }
