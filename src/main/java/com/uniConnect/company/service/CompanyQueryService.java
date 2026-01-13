package com.uniConnect.company.service;

import com.uniConnect.company.dto.CompanyCardResponse;
import com.uniConnect.company.dto.CompanyDetailResponse;
import com.uniConnect.company.entity.Company;
import com.uniConnect.company.entity.CompanyProfile;
import com.uniConnect.company.repository.CompanyProfileRepository;
import com.uniConnect.company.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyQueryService {

    private final CompanyRepository companyRepository;
    private final CompanyProfileRepository profileRepository;

    /** 학생 단체용 기업 리스트 조회 */
    public List<CompanyCardResponse> getCompanyList() {

        List<Company> companies = companyRepository.findAllWithProfileAndIndustry();

        return companies.stream().map(company -> {

            CompanyProfile profile =
                    profileRepository.findByCompanyCompanyId(company.getCompanyId()).orElse(null);

            String description = (profile != null && profile.getDescription() != null)
                    ? profile.getDescription()
                    : "";

            String shortDesc = description.length() > 40
                    ? description.substring(0, 40) + "..."
                    : description;

            return CompanyCardResponse.builder()
                    .companyId(company.getCompanyId())
                    .brandName(company.getBrandName())
                    .logoUrl(company.getLogoUrl())
                    .shortDescription(shortDesc)
                    .industryName(
                            company.getIndustry() != null ? company.getIndustry().getName() : null
                    )
                    .used(false)
                    .build();
        }).toList();
    }

    /** 학생 단체 기업 상세 조회 */
    public CompanyDetailResponse getCompanyDetail(Long companyId) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("회사를 찾을 수 없습니다."));

        CompanyProfile profile =
                profileRepository.findByCompanyCompanyId(companyId).orElse(null);

        return CompanyDetailResponse.builder()
                .companyId(company.getCompanyId())
                .brandName(company.getBrandName())
                .logoUrl(company.getLogoUrl())
                .description(profile != null ? profile.getDescription() : null)
                .website(profile != null ? profile.getWebsite() : null)
                .snsUrl(profile != null ? profile.getSnsUrl() : null)
                .industryName(company.getIndustry() != null ? company.getIndustry().getName() : null)
                .build();
    }
}