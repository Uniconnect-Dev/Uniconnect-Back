package com.uniConnect.contract.dto;

import com.uniConnect.contract.entity.Contract;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractSignResponseDto {
    private Long contractId;
    private String status;
    private Boolean studentSigned;
    private LocalDateTime studentSignedAt;
    private String signatureFileUrl;

    public static ContractSignResponseDto fromEntity(Contract contract) {
        if (contract == null) return null;

        return ContractSignResponseDto.builder()
                .contractId(contract.getContractId())
                .status(contract.getStatus() != null ? contract.getStatus().name() : null)
                .studentSigned(contract.getStudentSigned())
                .studentSignedAt(contract.getStudentSignedAt())
                .signatureFileUrl(contract.getSignatureFileUrl())
                .build();
    }
}
