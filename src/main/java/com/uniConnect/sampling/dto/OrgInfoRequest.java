package com.uniConnect.sampling.dto.request;

import jakarta.validation.constraints.*;

public record OrgInfoRequest(
        @NotBlank String schoolName,
        @NotBlank String orgName,
        @NotBlank String contactName,
        @Pattern(regexp = "^[0-9\\-]{9,15}$") String phone,
        @Email @NotBlank String email
) {}
