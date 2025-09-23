package com.uniConnect.s3.dto;

import jakarta.validation.constraints.*;

public record DeleteRequest(
        @NotBlank @Size(max = 512)
        @Pattern(regexp = "^[a-zA-Z0-9._\\-/]+$")
        String key) {
}
