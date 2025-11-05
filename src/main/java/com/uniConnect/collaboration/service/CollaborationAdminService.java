package com.uniConnect.collaboration.service;

import com.uniConnect.collaboration.dto.SendContractRequest;
import com.uniConnect.collaboration.entity.Collaboration;
import com.uniConnect.collaboration.entity.CollaborationReport;
import com.uniConnect.collaboration.entity.ReceiptConfirmation;
import com.uniConnect.collaboration.enums.CollaborationStatus;
import com.uniConnect.collaboration.enums.ReportStatus;
import com.uniConnect.collaboration.enums.ReceiptStatus;
import com.uniConnect.collaboration.repository.CollaborationReportRepository;
import com.uniConnect.collaboration.repository.CollaborationRepository;
import com.uniConnect.collaboration.repository.ReceiptConfirmationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 관리자용 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CollaborationAdminService {

    private final CollaborationRepository collaborationRepository;
    private final ReceiptConfirmationRepository receiptRepository;
    private final CollaborationReportRepository reportRepository;

    /** 협업 전체 목록 */
    public List<Collaboration> findAll() {
        return collaborationRepository.findAllWithDetails();
    }

    /** 계약서 전송 */
    public void sendContract(Long collaborationId, SendContractRequest request) {
        Collaboration c = getOrThrow(collaborationId);
        c.setContractUrl(request.getContractUrl());
        // 계약 시작이므로 Ready 유지
        c.setStatus(CollaborationStatus.ContractPending);
        collaborationRepository.save(c);
    }

    /** 계약 승인 (학생이 서명한 뒤) */
    public void approveContract(Long collaborationId) {
        Collaboration c = getOrThrow(collaborationId);
        c.setAdminContractApprovedAt(LocalDateTime.now());
        // 인수증 단계로 진입
        c.setStatus(CollaborationStatus.ReceiptPending);
        collaborationRepository.save(c);
    }

    /** 인수증 승인 (가장 최근 인수증 승인) */
    public void approveLatestReceipt(Long collaborationId) {
        // 1. 협업 찾기
        Collaboration c = getOrThrow(collaborationId);

        // 2. 이 협업에 속한 가장 최근 인수증 하나 가져오기
        ReceiptConfirmation latest = receiptRepository
                .findTopByCollaborationOrderBySubmittedAtDesc(c)
                .orElseThrow(() -> new IllegalStateException("승인할 인수증이 없습니다."));

        // 3. 인수증 상태 변경
        latest.setApprovedAt(LocalDateTime.now());
        latest.setStatus(ReceiptStatus.Approved);
        receiptRepository.save(latest);

        // 4. 협업 상태를 리포트 단계로 이동
        c.setStatus(CollaborationStatus.ReportPending);
        collaborationRepository.save(c);
    }

    /** 리포트 승인 */
    public void approveReport(Long collaborationId, Long reportId) {
        CollaborationReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("리포트를 찾을 수 없습니다. id=" + reportId));

        report.setStatus(ReportStatus.Approved);
        report.setApprovedAt(LocalDateTime.now());
        reportRepository.save(report);

        Collaboration c = report.getCollaboration();
        c.setStatus(CollaborationStatus.SurveyPending);
        collaborationRepository.save(c);
    }

    /** 전체 완료 */
    public void complete(Long collaborationId) {
        Collaboration c = getOrThrow(collaborationId);
        c.setStatus(CollaborationStatus.Completed);
        collaborationRepository.save(c);
    }

    private Collaboration getOrThrow(Long id) {
        return collaborationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("협업을 찾을 수 없습니다. id=" + id));
    }
}