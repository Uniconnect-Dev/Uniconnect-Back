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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

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
        return studentOrgRepository.findById(studentOrgId)
                .map(StudentOrg::getOrganizationName)
                .orElse("알 수 없음");
    }

    private String getCompanyName(Long companyId) {
        return companyRepository.findById(companyId)
                .map(Company::getBrandName)
                .orElse("알 수 없음");
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
    public List<MatchSentItemResponse> getStudentOrgSentList(Long studentOrgId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        return repo.findByStudentOrgId(studentOrgId, pageable)
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
    public List<MatchReceivedItemResponse> getStudentOrgReceivedList(Long studentOrgId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        return repo.findByStudentOrgId(studentOrgId, pageable)
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

    public void approveMatchByStudent(Long studentOrgId, Long matchId) {
        CollaborationMatchRequest match = repo.findById(matchId)
                .orElseThrow(() -> new RuntimeException("매칭을 찾을 수 없습니다. id=" + matchId));

        if (!match.getStudentOrgId().equals(studentOrgId)) {
            throw new RuntimeException("해당 매칭을 승인할 권한이 없습니다.");
        }

        if (match.getStatus() != MatchingStatus.Requested) {
            throw new RuntimeException("이미 승인 또는 거절된 매칭입니다.");
        }

        match.setStatus(MatchingStatus.Approved);
        match.setRespondedAt(java.time.LocalDateTime.now());
        repo.save(match);
    }

    public void rejectMatchByStudent(Long studentOrgId, Long matchId) {
        CollaborationMatchRequest match = repo.findById(matchId)
                .orElseThrow(() -> new RuntimeException("매칭을 찾을 수 없습니다. id=" + matchId));

        if (!match.getStudentOrgId().equals(studentOrgId)) {
            throw new RuntimeException("해당 매칭을 거절할 권한이 없습니다.");
        }

        if (match.getStatus() != MatchingStatus.Requested) {
            throw new RuntimeException("이미 승인 또는 거절된 매칭입니다.");
        }

        match.setStatus(MatchingStatus.Rejected);
        match.setRespondedAt(java.time.LocalDateTime.now());
        repo.save(match);
    }


    // ============================
    // 기업 버전
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

    public List<MatchSentItemResponse> getCompanySentList(Long companyId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        return repo.findByCompanyId(companyId, pageable)
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


    // ============================
    // 기업: 받은 매칭 목록
    // ============================

    public List<MatchReceivedItemResponse> getCompanyReceivedList(Long companyId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        return repo.findByCompanyId(companyId, pageable)
                .stream()
                .map(m -> MatchReceivedItemResponse.builder()
                        .matchId(m.getId())
                        .senderId(m.getStudentOrgId())
                        .senderName(getOrgName(m.getStudentOrgId()))
                        .eventTitle(m.getEventTitle())
                        .collaborationType(m.getCollaborationType())
                        .desiredDate(m.getDesiredDate())
                        .requestedAt(m.getRequestedAt())
                        .build())
                .toList();
    }

    public void approveMatchByCompany(Long companyId, Long matchId) {
        CollaborationMatchRequest match = repo.findById(matchId)
                .orElseThrow(() -> new RuntimeException("매칭을 찾을 수 없습니다. id=" + matchId));

        if (!match.getCompanyId().equals(companyId)) {
            throw new RuntimeException("해당 매칭을 승인할 권한이 없습니다.");
        }

        if (match.getStatus() != MatchingStatus.Requested) {
            throw new RuntimeException("이미 승인 또는 거절된 매칭입니다.");
        }

        match.setStatus(MatchingStatus.Approved);
        match.setRespondedAt(java.time.LocalDateTime.now());
        repo.save(match);
    }

    public void rejectMatchByCompany(Long companyId, Long matchId) {
        CollaborationMatchRequest match = repo.findById(matchId)
                .orElseThrow(() -> new RuntimeException("매칭을 찾을 수 없습니다. id=" + matchId));

        if (!match.getCompanyId().equals(companyId)) {
            throw new RuntimeException("해당 매칭을 거절할 권한이 없습니다.");
        }

        if (match.getStatus() != MatchingStatus.Requested) {
            throw new RuntimeException("이미 승인 또는 거절된 매칭입니다.");
        }

        match.setStatus(MatchingStatus.Rejected);
        match.setRespondedAt(java.time.LocalDateTime.now());
        repo.save(match);
    }

}