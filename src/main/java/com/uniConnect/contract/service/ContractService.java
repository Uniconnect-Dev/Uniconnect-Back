
package com.uniConnect.contract.service;

import com.uniConnect.member.security.local.CustomUser;
import com.uniConnect.contract.dto.*;
import com.uniConnect.contract.entity.Contract;
import com.uniConnect.contract.enums.ContractStatus;
import com.uniConnect.contract.repository.ContractRepository;
import com.uniConnect.global.exception.CustomException;
import com.uniConnect.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;

    @Transactional(readOnly = true)
    public ContractResponseDto getContract(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTRACT_NOT_FOUND));
        return ContractResponseDto.fromEntity(contract);
    }

    @Transactional(readOnly = true)
    public List<ContractListItemDto> getMyContracts(CustomUser user) {
        String role = user.getAuthorities().iterator().next().getAuthority();

        List<Contract> contracts;
        if (role.equals("ROLE_ADMIN")) {
            contracts = contractRepository.findAll();
        } else {
            contracts = contractRepository.findByMatching_StudentOrg_StudentOrgId(user.getUsersId());
        }

        return contracts.stream().map(ContractListItemDto::fromEntity).collect(Collectors.toList());
    }

    @Transactional
    public ContractSignResponseDto signContract(Long contractId, ContractSignRequestDto requestDto) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTRACT_NOT_FOUND));

        if (Boolean.TRUE.equals(contract.getStudentSigned())) {
            throw new CustomException(ErrorCode.CONTRACT_ALREADY_SIGNED);
        }

        contract.markStudentSigned(requestDto.getSignatureFileUrl());
        contractRepository.save(contract);

        return ContractSignResponseDto.fromEntity(contract);
    }

    @Transactional(readOnly = true)
    public ContractPdfResponseDto getContractPdf(Long contractId) {
        Contract c = contractRepository.findById(contractId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTRACT_NOT_FOUND));
        return ContractPdfResponseDto.builder().contractId(c.getContractId()).pdfUrl(c.getPdfUrl()).build();
    }

    @Transactional(readOnly = true)
    public ReceiptPreviewResponseDto getReceipt(Long contractId) {
        Contract c = contractRepository.findById(contractId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTRACT_NOT_FOUND));
        return ReceiptPreviewResponseDto.builder()
                .contractId(c.getContractId())
                .receiptPdfUrl(c.getReceiptPdfUrl())
                .status(c.getStatus().name())
                .build();
    }

    @Transactional
    public ReceiptSignResponseDto signReceipt(Long contractId, ReceiptSignRequestDto dto) {
        Contract c = contractRepository.findById(contractId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTRACT_NOT_FOUND));
        c.markReceiptSigned(dto.getSignatureFileUrl());
        contractRepository.save(c);
        return ReceiptSignResponseDto.fromEntity(c);
    }

    @Transactional
    public ContractResponseDto updateStatus(Long contractId, String status) {
        Contract c = contractRepository.findById(contractId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTRACT_NOT_FOUND));
        c.setStatus(ContractStatus.valueOf(status));
        contractRepository.save(c);
        return ContractResponseDto.fromEntity(c);
    }
}
