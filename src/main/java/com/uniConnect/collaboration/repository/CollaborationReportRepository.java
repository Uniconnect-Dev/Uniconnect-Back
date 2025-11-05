package com.uniConnect.collaboration.repository;

import com.uniConnect.collaboration.entity.CollaborationReport;
import com.uniConnect.collaboration.enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CollaborationReportRepository extends JpaRepository<CollaborationReport, Long> {
    List<CollaborationReport> findByCollaboration_Id(Long collaborationId);
    List<CollaborationReport> findByStatus(ReportStatus status);
}