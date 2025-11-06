package com.uniConnect.profile.service;

import com.uniConnect.company.entity.Company;
import com.uniConnect.company.entity.Industry;
import com.uniConnect.company.repository.CompanyRepository;
import com.uniConnect.company.repository.IndustryRepository;
import com.uniConnect.global.exception.CustomException;
import com.uniConnect.global.exception.ErrorCode;
import com.uniConnect.member.entity.User;
import com.uniConnect.member.repository.UserRepository;
import com.uniConnect.profile.dto.CompanyInitRequest;
import com.uniConnect.profile.dto.CompanyGetResponse;
import com.uniConnect.profile.dto.CompanyUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyProfileService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final IndustryRepository industryRepository;

    @Transactional
    public CompanyGetResponse initProfile(Long userId, CompanyInitRequest request) {
        // 이미 프로필이 있는지 확인
        if (companyRepository.existsByUser_UserId(userId)) {
            throw new CustomException(ErrorCode.ALREADY_EXISTS);
        }

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // Industry 조회 (있으면)
        Industry industry = null;
        if (request.getIndustryId() != null) {
            industry = industryRepository.findById(request.getIndustryId())
                    .orElseThrow(() -> new CustomException(ErrorCode.INDUSTRY_NOT_FOUND));
        }

        // 새 Company 생성
        Company company = Company.builder()
                .brandName(request.getBrandName())
                .logoUrl(request.getLogoUrl())
                .mainContactId(request.getMainContactId())
                .user(user)
                .industry(industry)
                .build();

        Company saved = companyRepository.save(company);
        return toResponse(saved);
    }

    public CompanyGetResponse getProfile(Long userId) {
        Company company = companyRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROFILE_NOT_FOUND));
        return toResponse(company);
    }

    @Transactional
    public CompanyGetResponse updateProfile(Long userId, CompanyUpdateRequest request) {
        Company company = companyRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROFILE_NOT_FOUND));

        // 필드 업데이트
        if (request.getBrandName() != null) {
            company.setBrandName(request.getBrandName());
        }
        if (request.getLogoUrl() != null) {
            company.setLogoUrl(request.getLogoUrl());
        }
        if (request.getMainContactId() != null) {
            company.setMainContactId(request.getMainContactId());
        }
        if (request.getIndustryId() != null) {
            Industry industry = industryRepository.findById(request.getIndustryId())
                    .orElseThrow(() -> new CustomException(ErrorCode.INDUSTRY_NOT_FOUND));
            company.setIndustry(industry);
        }

        return toResponse(company);
    }

    private CompanyGetResponse toResponse(Company company) {
        return CompanyGetResponse.builder()
                .companyId(company.getCompanyId())
                .brandName(company.getBrandName())
                .logoUrl(company.getLogoUrl())
                .mainContactId(company.getMainContactId())
                .industryId(company.getIndustry() != null ? company.getIndustry().getIndustryId() : null)
                .industryName(company.getIndustry() != null ? company.getIndustry().getIndustryName() : null)
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }
}