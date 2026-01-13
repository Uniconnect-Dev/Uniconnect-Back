package com.uniConnect.profile.service;

import com.uniConnect.company.entity.BusinessType;
import com.uniConnect.company.entity.Company;
import com.uniConnect.company.entity.Industry;
import com.uniConnect.company.repository.BusinessTypeRepository;
import com.uniConnect.company.repository.CompanyRepository;
import com.uniConnect.company.repository.IndustryRepository;
import com.uniConnect.member.entity.User;
import com.uniConnect.member.repository.UserRepository;
import com.uniConnect.profile.dto.CompanyGetResponse;
import com.uniConnect.profile.dto.CompanyInitRequest;
import com.uniConnect.profile.dto.CompanyUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyProfileService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final IndustryRepository industryRepository;
    private final BusinessTypeRepository businessTypeRepository;

    /**
     * 프로필 초기 생성
     */
    public CompanyGetResponse initProfile(Long userId, CompanyInitRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (user.getCompany() != null) {
            throw new IllegalStateException("이미 회사 프로필이 존재합니다.");
        }

        // Industry 조회 또는 생성
        Industry industry = null;
        if (request.getIndustryType() != null) {
            industry = industryRepository.findByType(request.getIndustryType())
                    .orElseGet(() -> industryRepository.save(Industry.of(request.getIndustryType())));
        }

        // BusinessType 조회 또는 생성: name이 unique니 name기준
        BusinessType businessType = null;
        if (request.getBusinessType() != null) {
            String name= request.getBusinessType().getDisplayName();
            businessType = businessTypeRepository.findByName(name)
                    .orElseGet(() -> businessTypeRepository.save(BusinessType.of(request.getBusinessType())));
        }

        Company company = Company.builder()
                .brandName(request.getBrandName())
                .logoUrl(request.getLogoUrl())
                .mainContactId(request.getMainContactId())
                .industry(industry)
                .businessType(businessType)
                .build();

        companyRepository.save(company);

        user.setCompany(company);
        userRepository.save(user);

        return CompanyGetResponse.fromEntity(company);
    }

    /**
     * 프로필 조회
     */
    @Transactional(readOnly = true)
    public CompanyGetResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Company company = user.getCompany();
        if (company == null) {
            throw new IllegalStateException("회사 프로필이 존재하지 않습니다.");
        }

        return CompanyGetResponse.fromEntity(company);
    }

    /**
     * 프로필 업데이트
     */
    public CompanyGetResponse updateProfile(Long userId, CompanyUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Company company = user.getCompany();
        if (company == null) {
            throw new IllegalStateException("회사 프로필이 존재하지 않습니다.");
        }

        // 업종 업데이트
        if (request.getIndustryType() != null) {
            Industry industry = industryRepository.findByType(request.getIndustryType())
                    .orElseGet(() -> industryRepository.save(Industry.of(request.getIndustryType())));
            company.setIndustry(industry);
        }

        // 업태 업데이트
        if (request.getBusinessType() != null) {
            BusinessType businessType = businessTypeRepository.findByType(request.getBusinessType())
                    .orElseGet(() -> businessTypeRepository.save(BusinessType.of(request.getBusinessType())));
            company.setBusinessType(businessType);
        }

        // 기타 필드 업데이트
        if (request.getBrandName() != null) {
            company.setBrandName(request.getBrandName());
        }
        if (request.getLogoUrl() != null) {
            company.setLogoUrl(request.getLogoUrl());
        }
        if (request.getMainContactId() != null) {
            company.setMainContactId(request.getMainContactId());
        }
        if (request.getSamplingPurpose() != null) {
            company.setSamplingPurpose(request.getSamplingPurpose());
        }
        if (request.getSamplingStartDate() != null) {
            company.setSamplingStartDate(request.getSamplingStartDate());
        }
        if (request.getSamplingEndDate() != null) {
            company.setSamplingEndDate(request.getSamplingEndDate());
        }
        if (request.getProductName() != null) {
            company.setProductName(request.getProductName());
        }
        if (request.getProductCount() != null) {
            company.setProductCount(request.getProductCount());
        }

        return CompanyGetResponse.fromEntity(company);
    }
}