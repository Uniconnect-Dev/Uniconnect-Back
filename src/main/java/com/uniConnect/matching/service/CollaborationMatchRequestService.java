package com.uniConnect.matching.service;

import com.uniConnect.campaign.entity.Campaign;
import com.uniConnect.campaign.repository.CampaignRepository;
import com.uniConnect.collaboration.entity.Collaboration;
import com.uniConnect.collaboration.enums.CollaborationStatus;
import com.uniConnect.collaboration.repository.CollaborationRepository;
import com.uniConnect.company.entity.Company;
import com.uniConnect.company.repository.CompanyRepository;
import com.uniConnect.matching.entity.CollaborationMatchRequest;
import com.uniConnect.matching.enums.MatchSender;
import com.uniConnect.campaign.enums.MatchingStatus;
import com.uniConnect.matching.repository.CollaborationMatchRequestRepository;
import com.uniConnect.studentOrg.entity.StudentOrg;
import com.uniConnect.contract.repository.ContractRepository;
import com.uniConnect.contract.entity.Contract;
import com.uniConnect.contract.enums.ContractStatus;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CollaborationMatchRequestService {

    private final CollaborationMatchRequestRepository matchRepo;
    private final CollaborationRepository collaborationRepo;
    private final ContractRepository contractRepository;
    private final CampaignRepository campaignRepository;

    public void createMatchRequests(
            Long campaignId,
            Long userId,
            List<Long> companyIds
    ) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));

        if (!campaign.getStudentOrg().hasUser(userId)) {
            throw new IllegalStateException("권한이 없습니다.");
        }

        if (companyIds == null || companyIds.isEmpty()) {
            throw new IllegalArgumentException("선택된 기업이 없습니다.");
        }

        for (Long companyId : companyIds) {

            boolean exists =
                    matchRepo.existsByCampaign_CampaignIdAndCompany_CompanyId(
                            campaignId, companyId
                    );
            if (exists) continue;

            CollaborationMatchRequest match = CollaborationMatchRequest.builder()
                    .campaign(campaign)
                    .studentOrg(campaign.getStudentOrg())
                    .company(
                            Company.builder()
                                    .companyId(companyId)
                                    .build()
                    )
                    .sender(MatchSender.STUDENT_ORG)
                    .collaborationType(campaign.getCollaborationType())
                    .status(MatchingStatus.Requested)
                    .requestedAt(LocalDateTime.now())
                    .build();

            matchRepo.save(match);
        }
    }

    /**
     * 매칭 요청 승인
     */
    public void approve(Long matchId) {
        CollaborationMatchRequest match = matchRepo.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match request not found"));

        if (match.getStatus() != MatchingStatus.Requested) {
            throw new IllegalStateException("이미 처리된 매칭 요청입니다.");
        }

        if (collaborationRepo.existsByMatchRequest(match)) {
            throw new IllegalStateException("이미 협업이 생성된 매칭입니다.");
        }

        match.approve();

        Collaboration collaboration = Collaboration.builder()
                .matchRequest(match)
                .status(CollaborationStatus.ContractSent)
                .build();

        collaborationRepo.save(collaboration);

        Contract contract = Contract.builder()
                .collaboration(collaboration)
                .status(ContractStatus.PendingSignature)
                .build();

        contractRepository.save(contract);
    }

    /**
     * 매칭 요청 반려
     */
    public void reject(Long matchId) {
        CollaborationMatchRequest match = matchRepo.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match request not found"));

        if (match.getStatus() != MatchingStatus.Requested) {
            throw new IllegalStateException("이미 처리된 매칭 요청입니다.");
        }

        match.reject();
    }
}
