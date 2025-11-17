package com.uniConnect.sampling.dto.request;

import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonProperty;

public record OrgInfoRequest(
        @JsonProperty("schoolName")
        @NotBlank
        String schoolName,

        @JsonProperty("orgName")
        @NotBlank
        String orgName,

        @JsonProperty("contactName")
        @NotBlank
        String contactName,

        @JsonProperty("phone")
        @Pattern(regexp = "^[0-9\\-]{9,15}$")
        String phone,

        @JsonProperty("email")
        @Email @NotBlank
        String email
) {}
