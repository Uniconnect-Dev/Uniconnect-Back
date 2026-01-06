package com.uniConnect.matching.service;

import com.uniConnect.campaign.enums.MatchingStatus;
import com.uniConnect.company.entity.Company;
import com.uniConnect.company.repository.CompanyRepository;
import com.uniConnect.matching.dto.MatchReceivedItemResponse;
import com.uniConnect.matching.dto.MatchSentItemResponse;
import com.uniConnect.matching.dto.MatchStatusSummaryResponse;
import com.uniConnect.matching.entity.CollaborationMatchRequest;
import com.uniConnect.matching.enums.MatchSender;
import com.uniConnect.matching.repository.CollaborationMatchRequestRepository;
import com.uniConnect.studentOrg.entity.StudentOrg;
import com.uniConnect.studentOrg.repository.StudentOrgRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollaborationMatchQueryService {

    private final CollaborationMatchRequestRepository repo;
    private final StudentOrgRepository studentOrgRepository;
    private final CompanyRepository companyRepository;

    /* =========================
       사용자 → 소속 조회
    ========================= */

    public Long findStudentOrgIdByUserId(Long userId) {
        return studentOrgRepository.findByUsers_UserId(userId)
                .map(StudentOrg::getStudentOrgId)
                .orElseThrow(() -> new RuntimeException("학생단체 정보가 없습니다. userId=" + userId));
    }

    public Long findCompanyIdByUserId(Long userId) {
        return companyRepository.findByUsers_UserId(userId)
                .map(Company::getCompanyId)
                .orElseThrow(() -> new RuntimeException("기업 정보가 없습니다. userId=" + userId));
    }

    /* =========================
       학생단체 요약
    ========================= */

    public MatchStatusSummaryResponse getStudentOrgSummary(Long studentOrgId) {

        Long sent = repo.countByStudentOrg_StudentOrgIdAndSender(
                studentOrgId, MatchSender.STUDENT_ORG
        );
        Long received = repo.countByStudentOrg_StudentOrgIdAndSender(
                studentOrgId, MatchSender.COMPANY
        );

        Long approved = repo.countByStudentOrg_StudentOrgIdAndStatus(studentOrgId, MatchingStatus.Approved);
        Long pending = repo.countByStudentOrg_StudentOrgIdAndStatus(studentOrgId, MatchingStatus.Requested);
        Long rejected = repo.countByStudentOrg_StudentOrgIdAndStatus(studentOrgId, MatchingStatus.Rejected);

        return MatchStatusSummaryResponse.builder()
                .sentCount(sent)
                .receivedCount(received)
                .totalCount(sent + received)
                .approvedCount(approved)
                .pendingCount(pending)
                .rejectedCount(rejected)
                .build();
    }

    /* =========================
       학생단체: 보낸 매칭
    ========================= */

    public List<MatchSentItemResponse> getStudentOrgSentList(Long studentOrgId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        return repo.findByStudentOrg_StudentOrgIdAndSender(
                        studentOrgId,
                        MatchSender.STUDENT_ORG,
                        pageable
                )
                .stream()
                .map(m -> MatchSentItemResponse.builder()
                        .matchId(m.getId())
                        .targetId(m.getCompany().getCompanyId())
                        .targetName(m.getCompany().getBrandName())
                        .campaignTitle(m.getCampaign().getName())
                        .collaborationType(m.getCollaborationType().name())
                        .requestedAt(m.getRequestedAt())
                        .respondedAt(m.getRespondedAt())
                        .status(m.getStatus())
                        .build())
                .collect(Collectors.toList());
    }

    /* =========================
       학생단체: 받은 매칭
    ========================= */

    public List<MatchReceivedItemResponse> getStudentOrgReceivedList(Long studentOrgId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        return repo.findByStudentOrg_StudentOrgIdAndSender(
                        studentOrgId,
                        MatchSender.COMPANY,
                        pageable
                )
                .stream()
                .map(m -> MatchReceivedItemResponse.builder()
                        .matchId(m.getId())
                        .senderId(m.getCompany().getCompanyId())
                        .senderName(m.getCompany().getBrandName())
                        .campaignTitle(m.getCampaign().getName())
                        .collaborationType(m.getCollaborationType().name())
                        .requestedAt(m.getRequestedAt())
                        .build())
                .collect(Collectors.toList());
    }

    /* =========================
       승인 / 거절
    ========================= */

    public void approveMatchByStudent(Long studentOrgId, Long matchId) {
        CollaborationMatchRequest match = getMatch(matchId);

        if (!match.getStudentOrg().getStudentOrgId().equals(studentOrgId)) {
            throw new RuntimeException("승인 권한이 없습니다.");
        }

        updateStatus(match, MatchingStatus.Approved);
    }

    public void rejectMatchByStudent(Long studentOrgId, Long matchId) {
        CollaborationMatchRequest match = getMatch(matchId);

        if (!match.getStudentOrg().getStudentOrgId().equals(studentOrgId)) {
            throw new RuntimeException("거절 권한이 없습니다.");
        }

        updateStatus(match, MatchingStatus.Rejected);
    }

    public void approveMatchByCompany(Long companyId, Long matchId) {
        CollaborationMatchRequest match = getMatch(matchId);

        if (!match.getCompany().getCompanyId().equals(companyId)) {
            throw new RuntimeException("승인 권한이 없습니다.");
        }

        updateStatus(match, MatchingStatus.Approved);
    }

    public void rejectMatchByCompany(Long companyId, Long matchId) {
        CollaborationMatchRequest match = getMatch(matchId);

        if (!match.getCompany().getCompanyId().equals(companyId)) {
            throw new RuntimeException("거절 권한이 없습니다.");
        }

        updateStatus(match, MatchingStatus.Rejected);
    }

    /* =========================
       내부 유틸
    ========================= */

    private CollaborationMatchRequest getMatch(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("매칭을 찾을 수 없습니다. id=" + id));
    }

    private void updateStatus(CollaborationMatchRequest match, MatchingStatus status) {
        if (match.getStatus() != MatchingStatus.Requested) {
            throw new RuntimeException("이미 처리된 매칭입니다.");
        }
        match.setStatus(status);
        match.setRespondedAt(LocalDateTime.now());
    }
}
