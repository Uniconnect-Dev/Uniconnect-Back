package com.uniConnect.collaboration.service;

import com.uniConnect.collaboration.entity.Collaboration;
import com.uniConnect.collaboration.enums.CollaborationStatus;
import com.uniConnect.collaboration.repository.CollaborationRepository;
import com.uniConnect.collaboration.repository.CollaborationReportRepository;
import com.uniConnect.collaboration.enums.ReportStatus;
import com.uniConnect.collaboration.entity.CollaborationReport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class CollaborationService {

    private final CollaborationRepository collaborationRepository;
    private final CollaborationReportRepository collaborationReportRepository;


    private Collaboration getCollaboration(Long id) {
        return collaborationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Collaboration not found: " + id));
    }

    public void markRecommendationReady(Long collabId) {
        Collaboration col = getCollaboration(collabId);
        col.setStatus(CollaborationStatus.RecommendationReady);
    }

    // Step 2: 학생이 특정 기업에 매칭 요청하기
    public void requestMatching(Long collabId, Long companyId) {
        Collaboration col = getCollaboration(collabId);

        if (col.getMatching() != null) {
            col.getMatching().setSelectedCompanyId(companyId);
        }

        col.setStatus(CollaborationStatus.WaitingCompanyResponse);
    }

    // Step 3: 기업이 승인 → 계약 발송
    public void sendContract(Long collabId, String contractUrl) {
        Collaboration col = getCollaboration(collabId);
        col.setContractUrl(contractUrl);
        col.setStatus(CollaborationStatus.ContractSent);
    }

    // Step 3: 학생 서명 완료
    public void studentSign(Long collabId) {
        Collaboration col = getCollaboration(collabId);
        col.setStudentSignedAt(LocalDateTime.now());
        col.setStatus(CollaborationStatus.WaitingAdminApproval);
    }

    // Step 3: 어드민이 계약 승인
    public void adminApproveContract(Long collabId) {
        Collaboration col = getCollaboration(collabId);
        col.setAdminContractApprovedAt(LocalDateTime.now());
        col.setStatus(CollaborationStatus.WaitingReportUpload);
    }

    // Step 4: 리포트 제출
    public void uploadReport(Long collabId, String reportUrl) {
        Collaboration col = getCollaboration(collabId);

        CollaborationReport report = CollaborationReport.builder()
                .collaboration(col)
                .fileUrl(reportUrl)
                .status(ReportStatus.Submitted)
                .build();

        collaborationReportRepository.save(report);

        col.setStatus(CollaborationStatus.WaitingReportApproval);
    }

    // Step 4: 어드민 리포트 승인
    public void approveReport(Long collabId) {
        System.out.println("Loaded Collaboration class = " + Collaboration.class.getProtectionDomain().getCodeSource().getLocation());

        Collaboration col = getCollaboration(collabId);
        col.setStatus(CollaborationStatus.Completed);
    }
}
