package com.uniConnect.contract.service;

import com.uniConnect.contract.dto.ContractSignatureRequest;
import com.uniConnect.contract.entity.Contract;
import com.uniConnect.contract.enums.ContractStatus;
import com.uniConnect.contract.repository.ContractRepository;
import com.uniConnect.global.exception.CustomException;
import com.uniConnect.global.exception.ErrorCode;
import com.uniConnect.member.security.local.CustomUser;
import com.uniConnect.signature.dto.SignatureRequest;
import com.uniConnect.signature.service.SignatureService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ContractSignService {

    private final ContractRepository contractRepository;
    private final SignatureService signatureService;

    /**
     * 학생단체 계약서 서명
     */
    public void signByStudent(
            Long contractId,
            ContractSignatureRequest request
    ) {
        Contract contract = getContract(contractId);

        if (Boolean.TRUE.equals(contract.getStudentSigned())) {
            throw new CustomException(ErrorCode.CONTRACT_ALREADY_SIGNED);
        }

        // 전자서명 검증 및 해시 저장
        signatureService.saveSignature(
                SignatureRequest.builder()
                        .signatureImage(request.getSignatureBase64())
                        .timestamp(request.getTimestamp())
                        .build(),
                getCurrentUserId()
        );

        // 계약 상태 반영
        contract.setStudentSigned(true);
        contract.setStudentSignedAt(LocalDateTime.now());

        if (Boolean.TRUE.equals(contract.getCompanySigned())) {
            contract.setStatus(ContractStatus.Signed);
        } else {
            contract.setStatus(ContractStatus.StudentSigned);
        }
    }

    /**
     * 기업 계약서 서명
     */
    public void signByCompany(
            Long contractId,
            ContractSignatureRequest request
    ) {
        Contract contract = getContract(contractId);

        if (Boolean.TRUE.equals(contract.getCompanySigned())) {
            throw new CustomException(ErrorCode.CONTRACT_ALREADY_SIGNED);
        }

        signatureService.saveSignature(
                SignatureRequest.builder()
                        .signatureImage(request.getSignatureBase64())
                        .timestamp(request.getTimestamp())
                        .build(),
                getCurrentUserId()
        );

        contract.setCompanySigned(true);
        contract.setCompanySignedAt(LocalDateTime.now());

        if (Boolean.TRUE.equals(contract.getStudentSigned())) {
            contract.setStatus(ContractStatus.Signed);
        }
    }

    /**
     * 공통: 계약 조회
     */
    private Contract getContract(Long contractId) {
        return contractRepository.findById(contractId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.CONTRACT_NOT_FOUND)
                );
    }

    /**
     * 공통: 로그인 사용자 ID 조회
     */
    private Long getCurrentUserId() {
        CustomUser principal = (CustomUser)
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getPrincipal();
        return Long.valueOf(principal.getUserId());
    }
}
