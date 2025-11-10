package com.uniConnect.campaign.repository;

import com.uniConnect.campaign.entity.MatchingRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchingRequestRepository extends JpaRepository<MatchingRequest, Long> {
    boolean existsByMatchingIdAndStudentOrg_User_UserId(Long matchingId, Long userId);
}