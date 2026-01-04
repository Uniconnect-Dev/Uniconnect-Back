package com.uniConnect.payment.service;

import com.uniConnect.common.service.EncryptionService;
import com.uniConnect.company.entity.Company;
import com.uniConnect.company.repository.CompanyRepository;
import com.uniConnect.global.exception.CustomException;
import com.uniConnect.global.exception.ErrorCode;
import com.uniConnect.payment.dto.*;
import com.uniConnect.payment.entity.PaymentMethod;
import com.uniConnect.payment.enums.PaymentMethodType;
import com.uniConnect.payment.repository.*;
import com.uniConnect.sampling.repository.SamplingRequestRepository;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final CompanyRepository companyRepository;
    private final EncryptionService encryptionService;

    /**
     * 1. 결제 수단 등록 (카드 or 계좌)
     */
    public PaymentDto.PaymentMethodResponse registerPaymentMethod(
            Long companyId,
            PaymentDto.PaymentMethodRequest request) {

        // 1) 회사 정보 조회
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        // 2) PaymentMethod 생성
        PaymentMethod method = PaymentMethod.builder()
                .type(request.getType())
                .company(company)
                .build();

        // 3) 타입별 정보 저장 (민감 정보 암호화) //card, account
        if (request.getType() == PaymentMethodType.Card) {
            validateCardInfo(request);

            method.setCardNumberEnc(encryptionService.encrypt(request.getCardNumber()));
            method.setExpiry(request.getExpiry());
            method.setCvcEnc(encryptionService.encrypt(request.getCvc()));

            log.info("[결제 수단 등록] Company ID: {}, Type: CARD", companyId);

        } else if (request.getType() == PaymentMethodType.Account) {
            validateAccountInfo(request);

            method.setBankName(request.getBankName());
            method.setAccountNoEnc(encryptionService.encrypt(request.getAccountNumber()));
            method.setHolderName(request.getAccountHolder());

            log.info("[결제 수단 등록] Company ID: {}, Type: ACCOUNT, Bank: {}", companyId, request.getBankName());
        }

        // 4) DB에 저장
        PaymentMethod savedMethod = paymentMethodRepository.save(method);

        return convertToResponse(savedMethod);
    }

    /**
     * 2. 결제 수단 조회 - 기업의 모든 결제 수단
     */
    @Transactional(readOnly = true)
    public List<PaymentDto.PaymentMethodResponse> getPaymentMethods(Long companyId) {
        // 회사 존재 여부 확인
        companyRepository.findById(companyId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        log.info("[결제 수단 조회] Company ID: {}", companyId);

        return paymentMethodRepository.findByCompanyCompanyId(companyId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

//    /**
//     * 3. 결제 수단 조회 (단일)
//     */
//    @Transactional(readOnly = true)
//    public PaymentDto.PaymentMethodResponse getPaymentMethod(Long methodId, Long companyId) {
//        PaymentMethod method = paymentMethodRepository.findByMethodIdAndCompanyCompanyId(methodId, companyId)
//                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
//
//        return convertToResponse(method);
//    }

    /**
     * 4. 결제 수단 수정
     */
    public PaymentDto.PaymentMethodResponse updatePaymentMethod(
            Long methodId,
            Long companyId,
            PaymentDto.PaymentMethodRequest request) {

        PaymentMethod method = paymentMethodRepository.findByMethodIdAndCompanyCompanyId(methodId, companyId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        // 타입별 정보 업데이트
        if (request.getType() == PaymentMethodType.Card) {
            if (request.getCardNumber() != null && !request.getCardNumber().isEmpty()) {
                method.setCardNumberEnc(encryptionService.encrypt(request.getCardNumber()));
            }
            if (request.getExpiry() != null && !request.getExpiry().isEmpty()) {
                method.setExpiry(request.getExpiry());
            }
            if (request.getCvc() != null && !request.getCvc().isEmpty()) {
                method.setCvcEnc(encryptionService.encrypt(request.getCvc()));
            }

        } else if (request.getType() == PaymentMethodType.Account) {
            if (request.getBankName() != null && !request.getBankName().isEmpty()) {
                method.setBankName(request.getBankName());
            }
            if (request.getAccountNumber() != null && !request.getAccountNumber().isEmpty()) {
                method.setAccountNoEnc(encryptionService.encrypt(request.getAccountNumber()));
            }
            if (request.getAccountHolder() != null && !request.getAccountHolder().isEmpty()) {
                method.setHolderName(request.getAccountHolder());
            }
        }

        PaymentMethod updatedMethod = paymentMethodRepository.save(method);

        log.info("[결제 수단 수정] Method ID: {}, Company ID: {}", methodId, companyId);

        return convertToResponse(updatedMethod);
    }

    /**
     * 5. 결제 수단 삭제
     */
    public void deletePaymentMethod(Long methodId, Long companyId) {
        PaymentMethod method = paymentMethodRepository.findByMethodIdAndCompanyCompanyId(methodId, companyId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        paymentMethodRepository.delete(method);

        log.info("[결제 수단 삭제] Method ID: {}, Company ID: {}", methodId, companyId);
    }

    /**
     * 6. DTO 변환 헬퍼 메서드
     */
    private PaymentDto.PaymentMethodResponse convertToResponse(PaymentMethod method) {
        String displayInfo = "";

        if (method.getType() == PaymentMethodType.Card && method.getCardNumberEnc() != null) {
            // 카드: 마지막 4자리만 표시
            try {
                String decrypted = encryptionService.decrypt(method.getCardNumberEnc());
                String last4 = decrypted.substring(Math.max(0, decrypted.length() - 4));
                displayInfo = "카드 **** " + last4;
            } catch (Exception e) {
                log.warn("카드번호 복호화 실패");
                displayInfo = "카드 정보 불가";
            }

        } else if (method.getType() == PaymentMethodType.Account && method.getAccountNoEnc() != null) {
            // 계좌: 은행명과 마지막 4자리
            try {
                String decrypted = encryptionService.decrypt(method.getAccountNoEnc());
                String last4 = decrypted.substring(Math.max(0, decrypted.length() - 4));
                displayInfo = method.getBankName() + " **** " + last4;
            } catch (Exception e) {
                log.warn("계좌번호 복호화 실패");
                displayInfo = "계좌 정보 불가";
            }
        }

        return PaymentDto.PaymentMethodResponse.builder()
                .methodId(method.getMethodId())
                .type(method.getType())
                .displayInfo(displayInfo)  // 마스킹된 정보만 표시
                .holderName(method.getHolderName() != null ? method.getHolderName() : "")
                .build();
    }

    /**
     * 7. 카드 정보 검증
     */
    private void validateCardInfo(PaymentDto.PaymentMethodRequest request) {
        if (request.getCardNumber() == null || request.getCardNumber().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (request.getExpiry() == null || request.getExpiry().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (request.getCvc() == null || request.getCvc().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    /**
     * 8. 계좌 정보 검증
     */
    private void validateAccountInfo(PaymentDto.PaymentMethodRequest request) {
        if (request.getBankName() == null || request.getBankName().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (request.getAccountNumber() == null || request.getAccountNumber().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (request.getAccountHolder() == null || request.getAccountHolder().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}