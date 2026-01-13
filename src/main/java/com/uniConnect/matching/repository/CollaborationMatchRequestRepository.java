package com.uniConnect.matching.repository;

import com.uniConnect.matching.entity.CollaborationMatchRequest;
import com.uniConnect.campaign.enums.MatchingStatus;
import com.uniConnect.matching.enums.MatchSender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
//기업,학생 샘플링, 협업
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

    // 기업 기준 Sender로 조회 (List 반환)
    List<CollaborationMatchRequest> findByCompany_CompanyIdAndSender(
            Long companyId,
            MatchSender sender
    );

    // 동적 Company 조회를 위한 Custom 메서드
    @Query("""
        SELECT cmr FROM CollaborationMatchRequest cmr
        WHERE (cmr.company.companyId = :companyId 
               OR (cmr.samplingProposal IS NOT NULL AND cmr.samplingProposal.creator.company.companyId = :companyId)
               OR (cmr.collaborationProposal IS NOT NULL AND cmr.collaborationProposal.company.companyId = :companyId))
        AND cmr.sender = :sender
        ORDER BY cmr.requestedAt DESC
    """)

    List<CollaborationMatchRequest> findAllCompanyMatchingsBySender(
            @Param("companyId") Long companyId,
            @Param("sender") MatchSender sender
    );
    // 동적 Company 조회를 위한 Custom 메서드
    @Query("""
        SELECT cmr FROM CollaborationMatchRequest cmr
        WHERE (cmr.company.companyId = :companyId 
               OR (cmr.samplingProposal IS NOT NULL AND cmr.samplingProposal.creator.company.companyId = :companyId)
               OR (cmr.collaborationProposal IS NOT NULL AND cmr.collaborationProposal.company.companyId = :companyId))
        AND cmr.sender = :sender
        ORDER BY cmr.requestedAt DESC
    """)

    List<CollaborationMatchRequest> findAllCompanyMatchingsBySenderAndStatus(
            @Param("companyId") Long companyId,
            @Param("sender") MatchSender sender,
            @Param("status") MatchingStatus status
    );

    // 학생단체 기준 Sender로 조회 (List 반환)
    List<CollaborationMatchRequest> findByStudentOrg_StudentOrgIdAndSender(
            Long studentOrgId,
            MatchSender sender
    );

    // 동적 StudentOrg 조회를 위한 Custom 메서드
    @Query("""
        SELECT cmr FROM CollaborationMatchRequest cmr
        WHERE (cmr.studentOrg.studentOrgId = :studentOrgId 
               OR (cmr.campaign IS NOT NULL AND cmr.campaign.studentOrg.studentOrgId = :studentOrgId))
        AND cmr.sender = :sender
        ORDER BY cmr.requestedAt DESC
    """)
    List<CollaborationMatchRequest> findAllStudentOrgMatchingsBySender(
            @Param("studentOrgId") Long studentOrgId,
            @Param("sender") MatchSender sender
    );

    @Query("""
        SELECT cmr FROM CollaborationMatchRequest cmr
        WHERE (cmr.studentOrg.studentOrgId = :studentOrgId 
               OR (cmr.campaign IS NOT NULL AND cmr.campaign.studentOrg.studentOrgId = :studentOrgId))
        AND cmr.sender = :sender
        AND cmr.status = :status
        ORDER BY cmr.requestedAt DESC
    """)
    List<CollaborationMatchRequest> findAllStudentOrgMatchingsBySenderAndStatus(
            @Param("studentOrgId") Long studentOrgId,
            @Param("sender") MatchSender sender,
            @Param("status") MatchingStatus status
    );
}
