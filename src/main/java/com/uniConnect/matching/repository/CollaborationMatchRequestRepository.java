package com.uniConnect.matching.repository;

import com.uniConnect.matching.entity.CollaborationMatchRequest;
import com.uniConnect.campaign.enums.MatchingStatus;
import com.uniConnect.matching.enums.MatchSender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CollaborationMatchRequestRepository extends JpaRepository<CollaborationMatchRequest, Long> {

    void deleteByCampaign_CampaignId(Long campaignId);

    void deleteBySamplingProposal_ProposalId(Long proposalId);

    /* ===== 학생단체 기준 ===== */

    long countByStudentOrg_StudentOrgIdAndSender(
            Long studentOrgId,
            MatchSender sender
    );

    long countByStudentOrg_StudentOrgIdAndStatus(
            Long studentOrgId,
            MatchingStatus status
    );

    Page<CollaborationMatchRequest> findByStudentOrg_StudentOrgIdAndSender(
            Long studentOrgId,
            MatchSender sender,
            Pageable pageable
    );

    /* ===== 기업 기준 ===== */

    long countByCompany_CompanyIdAndSender(
            Long companyId,
            MatchSender sender
    );

    long countByCompany_CompanyIdAndStatus(
            Long companyId,
            MatchingStatus status
    );

    Page<CollaborationMatchRequest> findByCompany_CompanyIdAndSender(
            Long companyId,
            MatchSender sender,
            Pageable pageable
    );

    boolean existsByCampaign_CampaignIdAndCompany_CompanyId(
            Long campaignId,
            Long companyId
    );

    List<CollaborationMatchRequest>
    findByStudentOrg_StudentOrgIdAndStatus(
            Long studentOrgId,
            MatchingStatus status
    );

    List<CollaborationMatchRequest>
    findByCompany_CompanyIdAndStatus(
            Long companyId,
            MatchingStatus status
    );
}
