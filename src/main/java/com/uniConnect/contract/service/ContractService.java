package com.uniConnect.contract.service;

import com.uniConnect.contract.dto.*;
import com.uniConnect.contract.entity.Contract;
import com.uniConnect.contract.enums.ContractStatus;
import com.uniConnect.contract.repository.ContractRepository;
import com.uniConnect.global.exception.CustomException;
import com.uniConnect.global.exception.ErrorCode;
import com.uniConnect.member.entity.LocalCredential;
import com.uniConnect.member.entity.User;
import com.uniConnect.member.repository.LocalCredentialRepository;
import com.uniConnect.studentOrg.entity.StudentOrg;
import com.uniConnect.studentOrg.repository.StudentOrgRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;
    private final LocalCredentialRepository localCredentialRepository;
    private final StudentOrgRepository studentOrgRepository;

    /**
     * 로그인한 사용자(JWT loginId 기반)의 계약 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ContractListItemDto> getMyContracts() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loginId = (String) authentication.getPrincipal(); // ✅ JWT sub = loginId

        User user = localCredentialRepository.findByLoginId(loginId)
                .map(LocalCredential::getUser)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 loginId의 사용자를 찾을 수 없습니다."));

        StudentOrg org = studentOrgRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 사용자가 속한 단체를 찾을 수 없습니다."));

        List<Contract> contracts = contractRepository.findByMatching_StudentOrg_StudentOrgId(org.getStudentOrgId());

        return contracts.stream()
                .map(ContractListItemDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 계약 상세 조회
     */
    @Transactional(readOnly = true)
    public ContractResponseDto getContract(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTRACT_NOT_FOUND));
        return ContractResponseDto.fromEntity(contract);
    }

    /**
     * 계약서 서명 제출
     */
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

    /**
     * 계약서 PDF 조회
     */
    @Transactional(readOnly = true)
    public ContractPdfResponseDto getContractPdf(Long contractId) {
        Contract c = contractRepository.findById(contractId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTRACT_NOT_FOUND));
        return ContractPdfResponseDto.builder()
                .contractId(c.getContractId())
                .pdfUrl(c.getPdfUrl())
                .build();
    }

    /**
     * 인수증 조회
     */
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

    /**
     * 인수증 서명 제출
     */
    @Transactional
    public ReceiptSignResponseDto signReceipt(Long contractId, ReceiptSignRequestDto dto) {
        Contract c = contractRepository.findById(contractId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTRACT_NOT_FOUND));
        c.markReceiptSigned(dto.getSignatureFileUrl());
        contractRepository.save(c);
        return ReceiptSignResponseDto.fromEntity(c);
    }

    /**
     * 계약 상태 변경 (관리자용)
     */
    @Transactional
    public ContractResponseDto updateStatus(Long contractId, String status) {
        Contract c = contractRepository.findById(contractId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTRACT_NOT_FOUND));
        c.setStatus(ContractStatus.valueOf(status));
        contractRepository.save(c);
        return ContractResponseDto.fromEntity(c);
    }
}
