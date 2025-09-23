package com.uniConnect.s3.dto;

import jakarta.validation.constraints.*;

public record ListRequest(
        @Size(max = 256)
        @Pattern(regexp = "^[a-zA-Z0-9._\\-/]*$")
        String prefix
) { }
