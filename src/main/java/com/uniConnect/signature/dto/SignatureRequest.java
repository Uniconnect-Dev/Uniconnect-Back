package com.uniConnect.signature.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignatureRequest {

    @NotBlank
    private String signatureImage;

    @NotNull
    private Long timestamp;

}
