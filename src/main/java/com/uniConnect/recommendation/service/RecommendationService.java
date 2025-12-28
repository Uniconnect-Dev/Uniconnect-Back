package com.uniConnect.recommendation.service;

import com.uniConnect.campaign.entity.CampaignTarget;
import com.uniConnect.campaign.repository.CampaignTargetRepository;
import com.uniConnect.company.entity.Company;
import com.uniConnect.member.entity.User;
import com.uniConnect.studentOrg.entity.StudentOrg;
import com.uniConnect.company.repository.CompanyProfileRepository;
import com.uniConnect.company.repository.CompanyRepository;
import com.uniConnect.recommendation.dto.RecommendedCompanyResponse;
import com.uniConnect.studentOrg.entity.Hashtag;
import com.uniConnect.studentOrg.repository.StudentOrgKeywordRepository;
import com.uniConnect.member.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private final StudentOrgKeywordRepository studentOrgKeywordRepository;
    private final CompanyRepository companyRepository;
    private final CompanyProfileRepository companyProfileRepository;
    private final CampaignTargetRepository campaignTargetRepository;
    private final UserRepository userRepository;

    public List<RecommendedCompanyResponse> getRecommendedCompanies(Long userId, int selectedCount) {

        // 1) user 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        // 2) user → studentOrg 조회
        StudentOrg studentOrg = user.getStudentOrg();
        if (studentOrg == null) {
            throw new RuntimeException("학생단체 정보 없음");
        }
        Long studentOrgId = studentOrg.getStudentOrgId();

        // 3) 학생단체 해시태그 조회
        List<Long> studentTags = studentOrgKeywordRepository.findHashtagIdsByStudentOrg(studentOrgId);
        if (studentTags.isEmpty()) return Collections.emptyList();

        // 4) 전체 기업 조회
        List<Company> companies = companyRepository.findAll();

        List<RecommendedCompanyResponse> result = new ArrayList<>();

        for (Company company : companies) {

            List<CampaignTarget> targets = campaignTargetRepository.findByCampaign_Company(company);

            List<String> matched = new ArrayList<>();

            for (CampaignTarget ct : targets) {
                Hashtag tag = ct.getHashtag();

                if (tag != null && studentTags.contains(tag.getHashtagId())) {
                    matched.add(tag.getName());
                }

                if (matched.size() == 2) break;
            }

            if (matched.isEmpty()) continue;

            // 기업 설명 조회
            String description = companyProfileRepository.findFirstByCompany(company)
                    .map(cp -> cp.getDescription())
                    .orElse(company.getIndustry() != null
                            ? company.getIndustry().getIndustryName()
                            : null);

            result.add(RecommendedCompanyResponse.builder()
                    .companyId(company.getCompanyId())
                    .companyName(company.getBrandName())
                    .logoUrl(company.getLogoUrl())
                    .description(description)
                    .matchedTags(matched)
                    .score(matched.size())
                    .disabled(selectedCount >= 5)
                    .build());
        }

        return result.stream()
                .sorted(Comparator.comparing(RecommendedCompanyResponse::getScore).reversed())
                .toList();
    }
}
