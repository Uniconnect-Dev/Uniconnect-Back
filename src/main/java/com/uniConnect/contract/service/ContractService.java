package com.uniConnect.contract.service;

import com.uniConnect.member.security.local.CustomUser;
import org.springframework.security.core.context.SecurityContextHolder;
import com.uniConnect.contract.dto.CompanySignRequestDto;
import com.uniConnect.contract.dto.CompanySignResponseDto;
import com.uniConnect.contract.dto.ContractListItemDto;
import com.uniConnect.contract.dto.ContractPdfResponseDto;
import com.uniConnect.contract.dto.ContractResponseDto;
import com.uniConnect.contract.dto.ContractSignRequestDto;
import com.uniConnect.contract.dto.ContractSignResponseDto;
import com.uniConnect.contract.dto.ReceiptPreviewResponseDto;
import com.uniConnect.contract.dto.ReceiptSignRequestDto;
import com.uniConnect.contract.dto.ReceiptSignResponseDto;
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
     * 계약서 PDF 미리보기
     */
    @Transactional(readOnly = true)
    public ContractPdfResponseDto getContractPdf(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTRACT_NOT_FOUND));

        return ContractPdfResponseDto.builder()
                .contractId(contract.getContractId())
                .pdfUrl(contract.getPdfUrl())
                .build();
    }


    /**
     *  서명 처리
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

    @Transactional(readOnly = true)
    public List<ContractListItemDto> getMyContracts() {
        // 현재 로그인한 사용자 정보 가져오기
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        String role = authentication.getAuthorities().iterator().next().getAuthority(); // ex: ROLE_ADMIN, ROLE_STUDENT_ORG
        Long studentOrgId = null;

        // 학생 단체인 경우 studentOrgId 가져오기
        if (role.equals("ROLE_STUDENT_ORG")) {
            var principal = authentication.getPrincipal();
            if (principal instanceof CustomUser user) {
                studentOrgId = user.getUsersId();
            } else {
                throw new CustomException(ErrorCode.UNAUTHORIZED);
            }
        }

        // 데이터 조회
        List<Contract> contracts;

        if (role.equals("ROLE_ADMIN")) {
            // 관리자면 전체 계약서 조회
            contracts = contractRepository.findAll();
        } else if (studentOrgId != null) {
            // 학생 단체면 자기 단체의 계약서만 조회
            contracts = contractRepository.findByMatching_StudentOrg_StudentOrgId(studentOrgId);
        } else {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        return contracts.stream()
                .map(contract -> {
                    String studentOrgName = null;
                    String campaignName = null;
                    String collaborationType = null;

                    if (contract.getMatching() != null) {
                        var matching = contract.getMatching();
                        if (matching.getStudentOrg() != null) {
                            studentOrgName = matching.getStudentOrg().getOrganizationName();
                        }
                        if (matching.getCampaign() != null) {
                            campaignName = matching.getCampaign().getName();
                            collaborationType = matching.getCampaign().getPurpose();
                        }
                    }

                    return ContractListItemDto.builder()
                            .contractId(contract.getContractId())
                            .studentOrgName(studentOrgName)
                            .campaignName(campaignName)
                            .collaborationType(collaborationType)
                            .status(contract.getStatus().name())
                            .studentSigned(contract.getStudentSigned())
                            .companySigned(contract.getCompanySigned())
                            .studentSignedAt(contract.getStudentSignedAt())
                            .companySignedAt(contract.getCompanySignedAt())
                            .pdfUrl(contract.getPdfUrl())
                            .receiptPdfUrl(contract.getReceiptPdfUrl())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public CompanySignResponseDto companySignContract(Long contractId, CompanySignRequestDto requestDto) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTRACT_NOT_FOUND));

        // 회사 서명 처리
        contract.markCompanySigned(requestDto.getSignatureFileUrl());
        contractRepository.save(contract);

        return CompanySignResponseDto.builder()
                .contractId(contract.getContractId())
                .status(contract.getStatus().name())
                .companySigned(contract.getCompanySigned())
                .companySignedAt(contract.getCompanySignedAt())
                .companySignatureFileUrl(contract.getCompanySignatureFileUrl())
                .build();
    }

    /**
     * 인수증 미리보기
     */
    @Transactional(readOnly = true)
    public ReceiptPreviewResponseDto getReceipt(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTRACT_NOT_FOUND));

        return ReceiptPreviewResponseDto.builder()
                .contractId(contract.getContractId())
                .receiptPdfUrl(contract.getReceiptPdfUrl())
                .status(contract.getStatus().name())
                .build();
    }

    /**
     * 인수증 서명 제출
     */
    @Transactional
    public ReceiptSignResponseDto signReceipt(Long contractId, ReceiptSignRequestDto requestDto) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTRACT_NOT_FOUND));

        // 인수증 서명 처리
        contract.markReceiptSigned(requestDto.getSignatureFileUrl());
        contractRepository.save(contract);

        return ReceiptSignResponseDto.builder()
                .contractId(contract.getContractId())
                .status(contract.getStatus().name()) // 기대: RECEIPT_SIGNED
                .receiptSignedAt(contract.getReceiptSignedAt())
                .receiptSignatureFileUrl(contract.getReceiptSignatureFileUrl())
                .message("전송이 완료되었습니다.")
                .build();
    }

    /**
     * 관리자용 계약 상태 변경
     */
    @Transactional
    public ContractResponseDto updateStatus(Long contractId, String newStatus) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTRACT_NOT_FOUND));

        contract.setStatus(ContractStatus.valueOf(newStatus)); // 유효성 체크 넣으면 더 안전

        contractRepository.save(contract);

        return ContractResponseDto.builder()
                .contractId(contract.getContractId())
                .status(contract.getStatus().name())
                .studentSigned(contract.getStudentSigned())
                .companySigned(contract.getCompanySigned())
                .studentSignedAt(contract.getStudentSignedAt())
                .companySignedAt(contract.getCompanySignedAt())
                .pdfUrl(contract.getPdfUrl())
                .signatureFileUrl(contract.getSignatureFileUrl())
                .companySignatureFileUrl(contract.getCompanySignatureFileUrl())
                .receiptPdfUrl(contract.getReceiptPdfUrl())
                .receiptSignatureFileUrl(contract.getReceiptSignatureFileUrl())
                .receiptSignedAt(contract.getReceiptSignedAt())
                .build();
    }
}
