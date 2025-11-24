package com.uniConnect.matching.repository;

import com.uniConnect.matching.entity.CollaborationMatchRequest;
import com.uniConnect.campaign.enums.MatchingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CollaborationMatchRequestRepository extends JpaRepository<CollaborationMatchRequest, Long> {

    // 학생단체 기준
    List<CollaborationMatchRequest> findByStudentOrgId(Long studentOrgId);
    Long countByStudentOrgId(Long studentOrgId);
    Long countByStudentOrgIdAndStatus(Long studentOrgId, MatchingStatus status);

    // 기업 기준
    List<CollaborationMatchRequest> findByCompanyId(Long companyId);
    Long countByCompanyId(Long companyId);
    Long countByCompanyIdAndStatus(Long companyId, MatchingStatus status);

    // 받은 요청 (역방향)
    List<CollaborationMatchRequest> findByCompanyIdAndStatus(Long companyId, MatchingStatus status);
}
