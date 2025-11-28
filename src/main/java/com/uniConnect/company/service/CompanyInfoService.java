package com.uniConnect.company.service;

import com.uniConnect.sampling.dto.request.CompanySamplingInfoRequest;
import com.uniConnect.member.repository.UserRepository;
import com.uniConnect.member.entity.User;
import com.uniConnect.company.entity.Company;
import com.uniConnect.company.entity.Industry;
import com.uniConnect.company.repository.CompanyRepository;
import com.uniConnect.company.repository.IndustryRepository;
import com.uniConnect.sampling.dto.request.CompanySamplingInfoRequest;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyInfoService {

    private final CompanyRepository companyRepository;
    private final IndustryRepository industryRepository;

    public void updateCompanyInfo(Long userId, CompanySamplingInfoRequest dto) {

        Company company = companyRepository.findByUsersUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("기업 정보를 찾을 수 없습니다."));

        if (dto.samplingStartDate().isAfter(dto.samplingEndDate())) {
            throw new IllegalArgumentException("샘플링 시작일은 종료일보다 늦을 수 없습니다.");
        }

        Industry industry = industryRepository.findById(dto.industryId())
                .orElseThrow(() -> new IllegalArgumentException("해당 산업군을 찾을 수 없습니다."));

        company.setIndustry(industry);
        company.setSamplingPurpose(dto.samplingPurpose());
        company.setSamplingStartDate(dto.samplingStartDate());
        company.setSamplingEndDate(dto.samplingEndDate());
        company.setProductName(dto.productName());
        company.setProductCount(dto.productCount());

        companyRepository.save(company);
    }

}
