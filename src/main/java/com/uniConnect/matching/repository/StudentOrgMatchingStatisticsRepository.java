package com.uniConnect.matching.repository;

import com.uniConnect.campaign.enums.MatchingStatus;
import com.uniConnect.matching.entity.CollaborationMatchRequest;
import com.uniConnect.matching.entity.StudentOrgMatchingStatistics;
import com.uniConnect.matching.enums.MatchSender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentOrgMatchingStatisticsRepository extends JpaRepository<StudentOrgMatchingStatistics, Long> {
    Optional<StudentOrgMatchingStatistics> findByStudentOrgId(Long studentOrgId);
}
