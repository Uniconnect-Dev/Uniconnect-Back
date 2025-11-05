package com.uniConnect.collaboration.service;

import com.uniConnect.collaboration.dto.CollaborationDetailResponse;
import com.uniConnect.collaboration.entity.Collaboration;
import com.uniConnect.collaboration.entity.CollaborationReport;
import com.uniConnect.collaboration.entity.ReceiptConfirmation;
import com.uniConnect.collaboration.enums.CollaborationStatus;
import com.uniConnect.collaboration.enums.ReceiptStatus;
import com.uniConnect.collaboration.enums.ReportStatus;
import com.uniConnect.collaboration.repository.CollaborationReportRepository;
import com.uniConnect.collaboration.repository.CollaborationRepository;
import com.uniConnect.collaboration.repository.ReceiptConfirmationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
/**
 * 학생용 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CollaborationWorkflowService {

    private final CollaborationRepository collaborationRepository;
    private final ReceiptConfirmationRepository receiptConfirmationRepository;
    private final CollaborationReportRepository collaborationReportRepository;

    /** 계약서 보기 */
    public CollaborationDetailResponse getContract(Long collaborationId) {
        Collaboration c = getOrThrow(collaborationId);
        return CollaborationDetailResponse.from(c);
    }

    /** 계약서 서명(학생단체) */
    public void signContract(Long collaborationId) {
        Collaboration c = getOrThrow(collaborationId);
        c.setStudentSignedAt(LocalDateTime.now());
        c.setStatus(CollaborationStatus.ContractApprovalPending);
        collaborationRepository.save(c);
    }

    /** 인수증 업로드 */
    public void uploadReceipt(Long collaborationId, String fileUrl, String receiverName, String location) {
        Collaboration c = getOrThrow(collaborationId);

        ReceiptConfirmation receipt = ReceiptConfirmation.builder()
                .collaboration(c)
                .receiverName(receiverName)
                .location(location)
                .receiptImageUrl(fileUrl)
                .submittedAt(LocalDateTime.now())
                .status(ReceiptStatus.WaitingApproval) // 학생이 올렸으니 승인 대기
                .build();

        receiptConfirmationRepository.save(receipt);

        c.setStatus(CollaborationStatus.ReceiptApprovalPending);
    }

    /** 리포트 업로드 */
    public void uploadReport(Long collaborationId, String fileUrl, String title) {
        Collaboration c = getOrThrow(collaborationId);

        CollaborationReport report = CollaborationReport.builder()
                .collaboration(c)
                .reportTitle(title)
                .fileUrl(fileUrl)
                .status(ReportStatus.Submitted)
                .build();

        collaborationReportRepository.save(report);

        c.setStatus(CollaborationStatus.ReportApprovalPending);
    }

    /** 설문 완료 */
    public void completeSurvey(Long collaborationId) {
        Collaboration c = getOrThrow(collaborationId);
        c.setStatus(CollaborationStatus.Completed);
        collaborationRepository.save(c);
    }

    private Collaboration getOrThrow(Long id) {
        return collaborationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("협업을 찾을 수 없습니다. id=" + id));
    }
}
