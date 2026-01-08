package com.uniConnect.contract.service;

import com.uniConnect.contract.dto.*;
import com.uniConnect.contract.entity.Contract;
import com.uniConnect.contract.enums.ContractStatus;
import com.uniConnect.contract.repository.ContractRepository;
import com.uniConnect.global.exception.CustomException;
import com.uniConnect.global.exception.ErrorCode;
import com.uniConnect.member.security.local.CustomUser;
import com.uniConnect.member.entity.LocalCredential;
import com.uniConnect.member.entity.User;
import com.uniConnect.member.repository.LocalCredentialRepository;
import com.uniConnect.studentOrg.entity.StudentOrg;
import com.uniConnect.studentOrg.repository.StudentOrgRepository;
import com.uniConnect.s3.S3FileService;
import org.springframework.beans.factory.annotation.Value;
import com.uniConnect.common.util.Base64ToMultipartFileUtil;
import com.uniConnect.common.util.ByteArrayMultipartFile;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.Duration;
import java.net.URL;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;
    private final LocalCredentialRepository localCredentialRepository;
    private final StudentOrgRepository studentOrgRepository;

    @Value("${app.s3.bucket}")
    private String bucket;

    private final S3FileService s3FileService;

    /**
     * 로그인한 사용자(JWT loginId 기반)의 계약 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ContractListItemDto> getMyContracts() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUser principal = (CustomUser) auth.getPrincipal();

        Long userId = Long.valueOf(principal.getUserId());
        User user = localCredentialRepository.findByUserUserId(userId)
                .map(LocalCredential::getUser)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        StudentOrg org = studentOrgRepository.findByUsers_UserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 사용자가 속한 단체를 찾을 수 없습니다."));

        List<Contract> contracts =
                contractRepository.findByCollaboration_MatchRequest_StudentOrg_StudentOrgId(
                        org.getStudentOrgId()
                );

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

        if (contract.getStatus() != ContractStatus.PendingSignature) {
            throw new CustomException(ErrorCode.CONTRACT_ALREADY_SIGNED);
        }

        String filename = "contract-signature-" + contractId + ".png";
        MultipartFile multipartFile = Base64ToMultipartFileUtil.convert(
                requestDto.getSignatureBase64(),
                filename
        );

        String key = "contract/signatures/" + filename;
        try {
            s3FileService.upload(bucket, key, multipartFile);
        } catch (Exception e) {
            throw new RuntimeException("S3 업로드 실패", e);
        }

        LocalDateTime signedAt = LocalDateTime.parse(requestDto.getSignedAt());

        contract.setStudentSigned(true);
        contract.setStudentSignedAt(signedAt);
        contract.setSignatureFileUrl(key);

        if (Boolean.TRUE.equals(contract.getCompanySigned())) {
            contract.setStatus(ContractStatus.Signed);
        } else {
            contract.setStatus(ContractStatus.StudentSigned);
        }

        contractRepository.save(contract);

        return ContractSignResponseDto.fromEntity(contract);
    }

    /**
     * 계약서 PDF 조회
     */
    @Transactional(readOnly = true)
    public ContractPdfResponseDto getContractPdf(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTRACT_NOT_FOUND));

        // S3 presigned URL 생성
        URL signedUrl = s3FileService.presignGet(
                bucket,
                contract.getPdfUrl(),
                Duration.ofMinutes(10)
        );

        return ContractPdfResponseDto.builder()
                .contractId(contract.getContractId())
                .pdfUrl(signedUrl.toString())  // 이제 presigned URL 반환
                .build();
    }

    /**
     * 인수증 조회
     */
    @Transactional(readOnly = true)
    public ReceiptPreviewResponseDto getReceipt(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTRACT_NOT_FOUND));

        URL signedUrl = s3FileService.presignGet(
                bucket,
                contract.getReceiptPdfUrl(),
                Duration.ofMinutes(10)
        );

        return ReceiptPreviewResponseDto.builder()
                .contractId(contract.getContractId())
                .receiptPdfUrl(signedUrl.toString())
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

        if (contract.getStatus() != ContractStatus.ReceiptPending) {
            throw new CustomException(ErrorCode.INVALID_RECEIPT_STATUS);
        }

        String filename = "receipt-signature-" + contractId + ".png";
        MultipartFile multipartFile = Base64ToMultipartFileUtil.convert(
                requestDto.getSignatureBase64(),
                filename
        );

        String key = "contract/receipt-signatures/" + filename;

        try {
            s3FileService.upload(bucket, key, multipartFile);
        } catch (Exception e) {
            throw new RuntimeException("S3 업로드 실패", e);
        }

        LocalDateTime signedAt = LocalDateTime.parse(requestDto.getSignedAt());

        contract.setReceiptSignatureFileUrl(key);
        contract.setReceiptSignedAt(signedAt);
        contract.setStatus(ContractStatus.ReceiptSigned);

        contractRepository.save(contract);

        return ReceiptSignResponseDto.fromEntity(contract);
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

    /**
     * 계약서 PDF 다운로드 URL 생성
     */
    public ContractPdfDownloadResponse getDownloadUrl(Long contractId) {

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.CONTRACT_NOT_FOUND)
                );

        Long collaborationId =
                contract.getCollaboration().getId();

        // S3 저장 규칙
        // contract/{collaborationId}.pdf
        String s3Key = "contract/" + collaborationId + ".pdf";

        URL presignedUrl = s3FileService.presignGet(
                bucket,
                s3Key,
                Duration.ofMinutes(10)
        );

        return ContractPdfDownloadResponse.builder()
                .downloadUrl(presignedUrl.toString())
                .build();
    }
}
