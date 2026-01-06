package com.uniConnect.recommendation.service;

import com.uniConnect.campaign.entity.Campaign;
import com.uniConnect.campaign.enums.CampaignStatus;
import com.uniConnect.campaign.repository.CampaignRepository;
import com.uniConnect.company.entity.Company;
import com.uniConnect.company.entity.CompanyProfile;
import com.uniConnect.company.repository.CompanyProfileRepository;
import com.uniConnect.recommendation.dto.RecommendedSamplingCompanyResponse;
import com.uniConnect.sampling.entity.SamplingProposal;
import com.uniConnect.sampling.entity.SamplingTargetSelection;
import com.uniConnect.sampling.enums.SamplingStatus;
import com.uniConnect.sampling.repository.SamplingProposalRepository;
import com.uniConnect.studentOrg.entity.Hashtag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SamplingCompanyRecommendationService {

    private final CampaignRepository campaignRepository;
    private final SamplingProposalRepository samplingProposalRepository;
    private final CompanyProfileRepository companyProfileRepository;

    public List<RecommendedSamplingCompanyResponse> getRecommendedCompaniesByLoginUser(
            Long userId
    ) {
        // 1️⃣ 로그인 사용자 → 학생단체의 최신 캠페인
        Campaign campaign =
                campaignRepository
                        .findTopByStudentOrg_Users_UserIdAndStatusOrderByCreatedAtDesc(
                                userId,
                                CampaignStatus.Draft
                        )
                        .orElseThrow(() ->
                                new IllegalStateException("추천에 사용할 캠페인이 없습니다.")
                        );

        // 2️⃣ 캠페인 키워드
        Set<String> campaignKeywords =
                campaign.getCampaignTargets().stream()
                        .map(ct -> ct.getHashtag().getName())
                        .collect(Collectors.toSet());

        if (campaignKeywords.isEmpty()) {
            return List.of();
        }

        List<SamplingProposal> proposals =
                samplingProposalRepository.findByStatus(SamplingStatus.Submitted);

        List<RecommendedSamplingCompanyResponse> result = new ArrayList<>();

        for (SamplingProposal proposal : proposals) {

            Company company = proposal.getCreator().getCompany();
            if (company == null) continue;

            List<String> companyKeywords =
                    proposal.getSelections().stream()
                            .map(SamplingTargetSelection::getSelectedLabel)
                            .toList();

            List<String> matched =
                    companyKeywords.stream()
                            .filter(campaignKeywords::contains)
                            .toList();

            if (matched.isEmpty()) continue;

            String description =
                    companyProfileRepository
                            .findFirstByCompany(company)
                            .map(CompanyProfile::getDescription)
                            .orElse(null);

            result.add(
                    RecommendedSamplingCompanyResponse.builder()
                            .companyId(company.getCompanyId())
                            .brandName(company.getBrandName())
                            .logoUrl(company.getLogoUrl())
                            .description(description)
                            .matchedKeywords(matched)
                            .score(matched.size())
                            .build()
            );
        }

        return result.stream()
                .sorted(
                        Comparator.comparingInt(
                                RecommendedSamplingCompanyResponse::getScore
                        ).reversed()
                )
                .toList();
    }
}
