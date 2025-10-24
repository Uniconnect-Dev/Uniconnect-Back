package com.uniConnect.contract.service;

import com.uniConnect.contract.dto.ContractResponseDto;
import com.uniConnect.contract.dto.ContractSignRequestDto;
import com.uniConnect.contract.dto.ContractSignResponseDto;
import com.uniConnect.contract.entity.Contract;
import com.uniConnect.contract.repository.ContractRepository;
import com.uniConnect.global.exception.CustomException;
import com.uniConnect.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;

    /**
     * 계약서 상세 조회
     * 엔티티를 직접 반환하지 않고 DTO로 변환
     */
    @Transactional(readOnly = true)
    public ContractResponseDto getContract(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTRACT_NOT_FOUND));

        // 엔티티 → DTO 변환
        return ContractResponseDto.builder()
                .contractId(contract.getContractId())
                .status(contract.getStatus().name())
                .studentSigned(contract.getStudentSigned())
                .companySigned(contract.getCompanySigned())
                .studentSignedAt(contract.getStudentSignedAt())
                .companySignedAt(contract.getCompanySignedAt())
                .pdfUrl(contract.getPdfUrl())
                .signatureFileUrl(contract.getSignatureFileUrl())
                .build();
    }

    /**
     *  서명 처리 (학생용)
     */
    @Transactional
    public ContractSignResponseDto signContract(Long contractId, ContractSignRequestDto requestDto) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTRACT_NOT_FOUND));

        if (Boolean.TRUE.equals(contract.getStudentSigned())) {
            throw new CustomException(ErrorCode.CONTRACT_ALREADY_SIGNED);
        }

        // 실제 서명 저장
        contract.markStudentSigned(requestDto.getSignatureFileUrl());
        contractRepository.save(contract);

        return ContractSignResponseDto.builder()
                .contractId(contract.getContractId())
                .status(contract.getStatus().name())
                .studentSigned(contract.getStudentSigned())
                .studentSignedAt(contract.getStudentSignedAt())
                .signatureFileUrl(contract.getSignatureFileUrl())
                .build();
    }
}
