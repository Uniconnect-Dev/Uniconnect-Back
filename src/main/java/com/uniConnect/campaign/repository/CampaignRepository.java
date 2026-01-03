package com.uniConnect.campaign.repository;

import com.uniConnect.campaign.entity.Campaign;
import com.uniConnect.studentOrg.enums.CollaborationType;
import com.uniConnect.campaign.enums.CampaignStatus;
import com.uniConnect.campaign.dto.CompanyCampaignListResponse;

import java.util.Optional;
import java.util.List;
import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    Optional<Campaign> findTopByStudentOrg_StudentOrgIdOrderByCreatedAtDesc(Long studentOrgId);

    Optional<Campaign> findTopByStudentOrg_Users_UserIdAndStatusOrderByCreatedAtDesc(
            Long userId,
            CampaignStatus status
    );

    @Query("""
    SELECT DISTINCT c
    FROM Campaign c
    WHERE c.collaborationType = :collaborationType
      AND c.status = :status
      AND c.startDate <= :proposalEndDate
      AND c.endDate >= :proposalStartDate
""")
    List<Campaign> findSamplingCampaignCandidates(
            @Param("proposalStartDate") LocalDate proposalStartDate,
            @Param("proposalEndDate") LocalDate proposalEndDate,
            @Param("collaborationType") CollaborationType collaborationType,
            @Param("status") CampaignStatus status
    );


    Optional<Campaign> findTopByStudentOrg_StudentOrgIdAndCollaborationTypeOrderByCreatedAtDesc(
            Long studentOrgId,
            CollaborationType collaborationType
    );

    List<Campaign> findByCampaignId(Long campaignId);

    Optional<Campaign>
    findTopByStudentOrg_StudentOrgIdAndStatusOrderByCreatedAtDesc(
            Long studentOrgId,
            CampaignStatus status
    );

    // 전체 조회
    @Query("""
    SELECT new com.uniConnect.campaign.dto.CompanyCampaignListResponse(
        c.campaignId,
        c.name,
        c.productQuantity,
        c.startDate,
        c.endDate,
        s.schoolName,
        s.organizationName,
        s.logoUrl
    )
    FROM Campaign c
    JOIN c.studentOrg s
""")
    Page<CompanyCampaignListResponse> findCampaigns(Pageable pageable);

    // 필터링 검색
    @Query("""
    SELECT new com.uniConnect.campaign.dto.CompanyCampaignListResponse(
        c.campaignId,
        c.name,
        c.productQuantity,
        c.startDate,
        c.endDate,
        s.schoolName,
        s.organizationName,
        s.logoUrl
    )
    FROM Campaign c
    JOIN c.studentOrg s
    WHERE (:keyword IS NULL 
            OR c.name LIKE %:keyword% 
            OR s.schoolName LIKE %:keyword%)
      AND (:collaborationType IS NULL 
            OR c.collaborationType = :collaborationType)
      AND (:startDate IS NULL OR c.startDate >= :startDate)
      AND (:endDate IS NULL OR c.endDate <= :endDate)
""")
    Page<CompanyCampaignListResponse> searchCampaigns(
            @Param("keyword") String keyword,
            @Param("collaborationType") CollaborationType collaborationType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    // 상세 조회
    @Query("""
        SELECT c
        FROM Campaign c
        JOIN FETCH c.studentOrg
        LEFT JOIN FETCH c.campaignTargets ct
        LEFT JOIN FETCH ct.hashtag
        WHERE c.campaignId = :campaignId
    """)
    Optional<Campaign> findDetailById(@Param("campaignId") Long campaignId);

}