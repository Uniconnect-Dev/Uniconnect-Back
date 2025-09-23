package com.uniConnect.s3.dto;

import jakarta.validation.constraints.*;

public record PresignGetRequest(
        @Size(max = 512)
        @Pattern(regexp = "^[a-zA-Z0-9._\\-/]+$")
        String key,

        @Positive @Max(3600)
        long expiresInSec
) { }
