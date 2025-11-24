package com.uniConnect.matching.service;

import com.uniConnect.campaign.enums.MatchingStatus;
import com.uniConnect.studentOrg.repository.StudentOrgRepository;
import com.uniConnect.studentOrg.entity.StudentOrg;
import com.uniConnect.company.entity.Company;
import com.uniConnect.company.repository.CompanyRepository;
import com.uniConnect.matching.dto.MatchReceivedItemResponse;
import com.uniConnect.matching.dto.MatchSentItemResponse;
import com.uniConnect.matching.dto.MatchStatusSummaryResponse;
import com.uniConnect.matching.entity.CollaborationMatchRequest;
import com.uniConnect.matching.repository.CollaborationMatchRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class CollaborationMatchQueryService {

    private final CollaborationMatchRequestRepository repo;
    private final StudentOrgRepository studentOrgRepository;
    private final CompanyRepository companyRepository;

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

    private String getOrgName(Long studentOrgId) {
        // TODO: StudentOrgRepository 연결
        return "학생단체명";
    }

    private String getCompanyName(Long companyId) {
        // TODO: CompanyRepository 연결
        return "기업명";
    }

    // ============================
    // 1) 학생단체 요약 조회
    // ============================
    public MatchStatusSummaryResponse getStudentOrgSummary(Long studentOrgId) {

        Long sent = repo.countByStudentOrgId(studentOrgId);

        Long approved = repo.countByStudentOrgIdAndStatus(studentOrgId, MatchingStatus.Approved);
        Long pending = repo.countByStudentOrgIdAndStatus(studentOrgId, MatchingStatus.Requested);
        Long rejected = repo.countByStudentOrgIdAndStatus(studentOrgId, MatchingStatus.Rejected);

        return MatchStatusSummaryResponse.builder()
                .sentCount(sent)
                .receivedCount(0L)
                .totalCount(sent)
                .approvedCount(approved)
                .pendingCount(pending)
                .rejectedCount(rejected)
                .build();
    }

    // ============================
    // 2) 학생단체: 내가 보낸 매칭 목록
    // ============================
    public List<MatchSentItemResponse> getStudentOrgSentList(Long studentOrgId) {

        return repo.findByStudentOrgId(studentOrgId)
                .stream()
                .map(m -> MatchSentItemResponse.builder()
                        .matchId(m.getId())
                        .targetId(m.getCompanyId())
                        .targetName(getCompanyName(m.getCompanyId()))
                        .eventTitle(m.getEventTitle())
                        .collaborationType(m.getCollaborationType())
                        .desiredDate(m.getDesiredDate())
                        .requestedAt(m.getRequestedAt())
                        .respondedAt(m.getRespondedAt())
                        .status(m.getStatus())
                        .build())
                .toList();
    }

    // ============================
    // 3) 학생단체: 받은 매칭 목록
    // ============================
    public List<MatchReceivedItemResponse> getStudentOrgReceivedList(Long studentOrgId) {

        return repo.findByCompanyId(studentOrgId) // 기업이 아니라 학생단체에게 온 요청
                .stream()
                .map(m -> MatchReceivedItemResponse.builder()
                        .matchId(m.getId())
                        .senderId(m.getCompanyId())
                        .senderName(getCompanyName(m.getCompanyId()))
                        .eventTitle(m.getEventTitle())
                        .collaborationType(m.getCollaborationType())
                        .desiredDate(m.getDesiredDate())
                        .requestedAt(m.getRequestedAt())
                        .build())
                .toList();
    }

    // ============================
    // 기업 버전도 동일하게 구현
    // ============================
    public MatchStatusSummaryResponse getCompanySummary(Long companyId) {

        Long sent = repo.countByCompanyId(companyId);
        Long approved = repo.countByCompanyIdAndStatus(companyId, MatchingStatus.Approved);
        Long pending = repo.countByCompanyIdAndStatus(companyId, MatchingStatus.Requested);
        Long rejected = repo.countByCompanyIdAndStatus(companyId, MatchingStatus.Rejected);

        return MatchStatusSummaryResponse.builder()
                .sentCount(sent)
                .receivedCount(0L)
                .totalCount(sent)
                .approvedCount(approved)
                .pendingCount(pending)
                .rejectedCount(rejected)
                .build();
    }

    public List<MatchSentItemResponse> getCompanySentList(Long companyId) {

        return repo.findByCompanyId(companyId)
                .stream()
                .map(m -> MatchSentItemResponse.builder()
                        .matchId(m.getId())
                        .targetId(m.getStudentOrgId())
                        .targetName(getOrgName(m.getStudentOrgId()))
                        .eventTitle(m.getEventTitle())
                        .collaborationType(m.getCollaborationType())
                        .desiredDate(m.getDesiredDate())
                        .requestedAt(m.getRequestedAt())
                        .respondedAt(m.getRespondedAt())
                        .status(m.getStatus())
                        .build())
                .toList();
    }

    public List<MatchReceivedItemResponse> getCompanyReceivedList(Long companyId) {

        return repo.findByCompanyId(companyId)
                .stream()
                .map(m -> MatchReceivedItemResponse.builder()
                        .matchId(m.getId())
                        .senderId(m.getStudentOrgId())
                        .senderName(getOrgName(m.getStudentOrgId()))
                        .eventTitle(m.getEventTitle())
                        .collaborationType(m.getCollaborationType())
                        .desiredDate(m.getDesiredDate())
                        .requestedAt(m.getRequestedAt())
                        .build()
                )
                .toList();
    }
}