package com.uniConnect.matching.service;

import com.uniConnect.campaign.entity.Campaign;
import com.uniConnect.campaign.enums.MatchingStatus;
import com.uniConnect.campaign.repository.CampaignRepository;

import com.uniConnect.company.entity.Company;
import com.uniConnect.company.repository.CompanyRepository;

import com.uniConnect.matching.dto.CompanyMatchRequest;
import com.uniConnect.matching.dto.CompanyMatchResponse;
import com.uniConnect.matching.dto.StudentOrgMatchRequestDto;
import com.uniConnect.matching.entity.CollaborationMatchRequest;
import com.uniConnect.matching.enums.MatchSender;
import com.uniConnect.matching.repository.CollaborationMatchRequestRepository;

import com.uniConnect.sampling.entity.SamplingProposal;
import com.uniConnect.sampling.repository.SamplingProposalRepository;

import com.uniConnect.partnership.entity.CollaborationProposal;
import com.uniConnect.partnership.repository.CollaborationProposalRepository;

import com.uniConnect.studentOrg.enums.CollaborationType;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CollaborationMatchService {

    private final CollaborationMatchRequestRepository matchRepo;
    private final CampaignRepository campaignRepository;
    private final CompanyRepository companyRepository;
    private final SamplingProposalRepository samplingProposalRepository;
    private final CollaborationProposalRepository collaborationProposalRepository;

    /* =====================================================
       1. 학생단체 → 기업 (Campaign 기반, 샘플링/장기협업 공통)
    ===================================================== */
    public void requestFromStudentOrg(
            Long userId,
            StudentOrgMatchRequestDto dto
    ) {
        Campaign campaign = campaignRepository.findById(dto.campaignId())
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));

        // 권한 체크
        if (!campaign.getStudentOrg().hasUser(userId)) {
            throw new IllegalStateException("권한이 없습니다.");
        }

        if (dto.companyIds() == null || dto.companyIds().isEmpty()) {
            throw new IllegalArgumentException("선택된 기업이 없습니다.");
        }

        // 재요청 허용: 기존 요청 삭제
        matchRepo.deleteByCampaign_CampaignId(campaign.getCampaignId());

        for (Long companyId : dto.companyIds()) {
            companyRepository.findById(companyId)
                    .orElseThrow(() -> new IllegalArgumentException("Company not found"));

            CollaborationMatchRequest match = CollaborationMatchRequest.builder()
                    .sender(MatchSender.STUDENT_ORG)
                    .collaborationType(campaign.getCollaborationType())
                    .campaign(campaign)
                    .status(MatchingStatus.Requested)
                    .requestedAt(LocalDateTime.now())
                    .build();

            matchRepo.save(match);
        }
    }

    /* =====================================================
       2. 기업 → 학생단체 (샘플링 / 장기협업)
    ===================================================== */
    public CompanyMatchResponse requestFromCompany(
            Long userId,
            CompanyMatchRequest dto
    ) {
        Company company = companyRepository.findByUsers_UserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        if (dto.targetIds() == null || dto.targetIds().isEmpty()) {
            throw new IllegalArgumentException("대상이 없습니다.");
        }

        int savedCount = 0;

        for (Long targetId : dto.targetIds()) {

            CollaborationMatchRequest match;

            if (dto.collaborationType() == CollaborationType.Sampling) {
                SamplingProposal proposal = samplingProposalRepository.findById(targetId)
                        .orElseThrow(() -> new IllegalArgumentException("SamplingProposal not found"));

                match = CollaborationMatchRequest.builder()
                        .sender(MatchSender.COMPANY)
                        .collaborationType(CollaborationType.Sampling)
                        .samplingProposal(proposal)
                        .status(MatchingStatus.Requested)
                        .requestedAt(LocalDateTime.now())
                        .build();

            } else {
                CollaborationProposal proposal = collaborationProposalRepository.findById(targetId)
                        .orElseThrow(() -> new IllegalArgumentException("CollaborationProposal not found"));

                match = CollaborationMatchRequest.builder()
                        .sender(MatchSender.COMPANY)
                        .collaborationType(CollaborationType.Partnership)
                        .collaborationProposal(proposal)
                        .status(MatchingStatus.Requested)
                        .requestedAt(LocalDateTime.now())
                        .build();
            }

            matchRepo.save(match);
            savedCount++;
        }

        return CompanyMatchResponse.builder()
                .total(savedCount)
                .message("기업 매칭 요청 완료")
                .build();
    }
}