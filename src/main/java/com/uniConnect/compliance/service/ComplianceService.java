package com.uniConnect.compliance.service;

import com.uniConnect.compliance.dto.ComplianceAgreementRequest;
import com.uniConnect.compliance.dto.ComplianceAgreementResponse;
import com.uniConnect.compliance.entity.ComplianceAgreement;
import com.uniConnect.compliance.entity.ComplianceAgreementDetail;
import com.uniConnect.compliance.enums.AgreementType;
import com.uniConnect.compliance.repository.ComplianceAgreementDetailRepository;
import com.uniConnect.compliance.repository.ComplianceAgreementRepository;
import com.uniConnect.global.exception.CustomException;
import com.uniConnect.global.exception.ErrorCode;
import com.uniConnect.member.entity.User;
import com.uniConnect.member.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ComplianceService {

    private final ComplianceAgreementRepository agreementRepository;
    private final ComplianceAgreementDetailRepository detailRepository;
    private final UserRepository userRepository;

    @Transactional
    public ComplianceAgreementResponse agreeToCompliance(
            Long userId,
            ComplianceAgreementRequest request,
            HttpServletRequest httpRequest
    ) {
        // 1. 중복 체크
        if (agreementRepository.existsByRequestId(request.getRequestId())) {
            throw new CustomException(ErrorCode.ALREADY_EXISTS);
        }

        // 2. User 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 3. 필수 동의 항목 검증
        validateRequiredAgreements(request.getAgreements());

        // 4. 동의 항목 매핑
        Map<AgreementType, Boolean> agreementMap = request.getAgreements().stream()
                .collect(Collectors.toMap(
                        ComplianceAgreementRequest.AgreementItem::getType,
                        ComplianceAgreementRequest.AgreementItem::getAccepted
                ));

        // 5. ComplianceAgreement 생성
        ComplianceAgreement agreement = ComplianceAgreement.builder()
                .requestId(request.getRequestId())
                .user(user)
                .processInfoAccepted(agreementMap.getOrDefault(AgreementType.PROCESS_INFO, false))
                .offplatformPenaltyAccepted(agreementMap.getOrDefault(AgreementType.OFFPLATFORM_PENALTY, false))
                .termsAckAccepted(agreementMap.getOrDefault(AgreementType.TERMS_ACK, false))
                .agreedAt(LocalDateTime.now())
                .ipAddress(getClientIp(httpRequest))
                .userAgent(httpRequest.getHeader("User-Agent"))
                .build();

        ComplianceAgreement savedAgreement = agreementRepository.save(agreement);

        // 6. 상세 내역 저장 (선택적)
        List<ComplianceAgreementDetail> details = request.getAgreements().stream()
                .map(item -> ComplianceAgreementDetail.builder()
                        .agreement(savedAgreement)
                        .agreementType(item.getType())
                        .accepted(item.getAccepted())
                        .description(item.getType().getDescription())
                        .build())
                .collect(Collectors.toList());
        detailRepository.saveAll(details);

        // 7. Response 생성
        return ComplianceAgreementResponse.builder()
                .requestId(savedAgreement.getRequestId())
                .agreementId(savedAgreement.getAgreementId())
                .status("COMPLETED")
                .agreementDetails(details.stream()
                        .map(detail -> ComplianceAgreementResponse.AgreementDetail.builder()
                                .type(detail.getAgreementType().name())
                                .accepted(detail.getAccepted())
                                .description(detail.getDescription())
                                .build())
                        .collect(Collectors.toList()))
                .agreedAt(savedAgreement.getAgreedAt())
                .message("샘플링 규정 동의가 완료되었습니다.")
                .build();
    }

    private void validateRequiredAgreements(List<ComplianceAgreementRequest.AgreementItem> agreements) {
        // 3개 모두 있는지 확인
        List<AgreementType> types = agreements.stream()
                .map(ComplianceAgreementRequest.AgreementItem::getType)
                .collect(Collectors.toList());

        if (!types.contains(AgreementType.PROCESS_INFO) ||
                !types.contains(AgreementType.OFFPLATFORM_PENALTY) ||
                !types.contains(AgreementType.TERMS_ACK)) {
            throw new CustomException(ErrorCode.COMPLIANCE_INCOMPLETE);
        }

        // 모두 true인지 확인
        boolean allAccepted = agreements.stream()
                .allMatch(ComplianceAgreementRequest.AgreementItem::getAccepted);

        if (!allAccepted) {
            throw new CustomException(ErrorCode.COMPLIANCE_INCOMPLETE);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    public ComplianceAgreementResponse getAgreementByRequestId(String requestId) {
        ComplianceAgreement agreement = agreementRepository.findByRequestId(requestId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMPLIANCE_AGREEMENT_NOT_FOUND));

        List<ComplianceAgreementDetail> details = detailRepository.findByAgreement_AgreementId(agreement.getAgreementId());

        return ComplianceAgreementResponse.builder()
                .requestId(agreement.getRequestId())
                .agreementId(agreement.getAgreementId())
                .status("COMPLETED")
                .agreementDetails(details.stream()
                        .map(detail -> ComplianceAgreementResponse.AgreementDetail.builder()
                                .type(detail.getAgreementType().name())
                                .accepted(detail.getAccepted())
                                .description(detail.getDescription())
                                .build())
                        .collect(Collectors.toList()))
                .agreedAt(agreement.getAgreedAt())
                .message("동의 내역 조회 완료")
                .build();
    }
}