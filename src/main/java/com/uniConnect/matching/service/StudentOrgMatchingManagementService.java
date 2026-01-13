package com.uniConnect.matching.service;

import com.uniConnect.campaign.enums.MatchingStatus;
import com.uniConnect.campaign.entity.Campaign;
import com.uniConnect.collaboration.entity.Collaboration;
import com.uniConnect.collaboration.repository.CollaborationRepository;
import com.uniConnect.company.entity.Company;
import com.uniConnect.contract.entity.Contract;
import com.uniConnect.contract.enums.ContractStatus;
import com.uniConnect.contract.repository.ContractRepository;
import com.uniConnect.matching.dto.*;
import com.uniConnect.matching.entity.CollaborationMatchRequest;
import com.uniConnect.matching.entity.StudentOrgMatchingStatistics;
import com.uniConnect.matching.enums.MatchSender;
import com.uniConnect.matching.repository.CollaborationMatchRequestRepository;
import com.uniConnect.matching.repository.StudentOrgMatchingStatisticsRepository;
import com.uniConnect.partnership.entity.CollaborationProposal;
import com.uniConnect.sampling.entity.SamplingProposal;
import com.uniConnect.studentOrg.entity.StudentOrg;
import com.uniConnect.studentOrg.repository.StudentOrgRepository;
import com.uniConnect.member.security.local.CustomUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentOrgMatchingManagementService {

    private final CollaborationMatchRequestRepository matchRequestRepository;
    private final StudentOrgMatchingStatisticsRepository statisticsRepository;
    private final CollaborationRepository collaborationRepository;
    private final ContractRepository contractRepository;
    private final StudentOrgRepository studentOrgRepository;

    /**
     * 학생단체의 전체 매칭 관리 페이지 조회
     */
    public StudentOrgMatchingStatisticsDto.StudentOrgMatchingPageResponse getMatchingManagementPage(
            StudentOrgMatchingStatisticsDto.StudentOrgMatchingFilterRequest filter,
            Pageable pageable
    ) {
        StudentOrg studentOrg = getStudentOrgFromLogin();

        // 1. 통계 조회
        StudentOrgMatchingStatisticsDto.StudentOrgMatchingStatisticsResponse statistics = getMatchingStatistics(studentOrg.getStudentOrgId());

        // 2. 요청한 매칭 조회 (STUDENT_ORG sender - Campaign 제안)
        List<StudentOrgMatchingStatisticsDto.StudentOrgMatchingManagementDto> sentMatchings =
                getSentMatchings(studentOrg, filter);

        // 3. 받은 요청 조회 (기업 Proposal - COMPANY sender)
        List<StudentOrgMatchingStatisticsDto.StudentOrgMatchingManagementDto> receivedMatchings =
                getReceivedMatchings(studentOrg, filter);

        // 페이징 처리
        long totalElements = sentMatchings.size() + receivedMatchings.size();
        int start = (int) pageable.getOffset();
        int end = Math.min((int) (start + pageable.getPageSize()), (int) totalElements);

        return StudentOrgMatchingStatisticsDto.StudentOrgMatchingPageResponse.builder()
                .statistics(statistics)
                .sentMatchings(sentMatchings)
                .receivedMatchings(receivedMatchings)
                .totalPages((int) Math.ceil((double) totalElements / pageable.getPageSize()))
                .currentPage(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .totalElements(totalElements)
                .build();
    }

    /**
     * 매칭 통계 조회
     */
    public StudentOrgMatchingStatisticsDto.StudentOrgMatchingStatisticsResponse getMatchingStatistics(Long studentOrgId) {
        // 모든 요청한 매칭 (학생단체가 보낸)
        Integer total = matchRequestRepository.findAllStudentOrgMatchingsBySender(
                studentOrgId,
                MatchSender.STUDENT_ORG
        ).size();

        // 완료된 매칭
        Integer completed = matchRequestRepository.findAllStudentOrgMatchingsBySenderAndStatus(
                studentOrgId,
                MatchSender.STUDENT_ORG,
                MatchingStatus.Approved
        ).size();

        // 대기 중
        Integer pending = matchRequestRepository.findAllStudentOrgMatchingsBySenderAndStatus(
                studentOrgId,
                MatchSender.STUDENT_ORG,
                MatchingStatus.Requested
        ).size();

        // 실패
        Integer failed = matchRequestRepository.findAllStudentOrgMatchingsBySenderAndStatus(
                studentOrgId,
                MatchSender.STUDENT_ORG,
                MatchingStatus.Rejected
        ).size();

        return StudentOrgMatchingStatisticsDto.StudentOrgMatchingStatisticsResponse.builder()
                .totalCount(total)
                .completedCount(completed)
                .pendingCount(pending)
                .failedCount(failed)
                .build();
    }

    /**
     * 학생단체가 요청한 매칭 조회
     * (STUDENT_ORG가 sender인 매칭 - Campaign 제안)
     */
    private List<StudentOrgMatchingStatisticsDto.StudentOrgMatchingManagementDto> getSentMatchings(
            StudentOrg studentOrg,
            StudentOrgMatchingStatisticsDto.StudentOrgMatchingFilterRequest filter
    ) {
        // 학생단체가 직접 보낸 매칭 + Campaign을 통한 매칭 포함
        List<CollaborationMatchRequest> matchRequests =
                matchRequestRepository.findAllStudentOrgMatchingsBySender(
                        studentOrg.getStudentOrgId(),
                        MatchSender.STUDENT_ORG
                );

        return matchRequests.stream()
                .filter(m -> applyFilters(m, filter))
                .sorted(Comparator.comparing(CollaborationMatchRequest::getRequestedAt).reversed())
                .map(this::toManagementDto)
                .collect(Collectors.toList());
    }

    /**
     * 학생단체가 받은 요청 조회
     * (COMPANY가 sender인 매칭 - SamplingProposal, CollaborationProposal)
     */
    private List<StudentOrgMatchingStatisticsDto.StudentOrgMatchingManagementDto> getReceivedMatchings(
            StudentOrg studentOrg,
            StudentOrgMatchingStatisticsDto.StudentOrgMatchingFilterRequest filter
    ) {
        List<CollaborationMatchRequest> matchRequests =
                matchRequestRepository.findAllStudentOrgMatchingsBySender(
                        studentOrg.getStudentOrgId(),
                        MatchSender.COMPANY
                );

        return matchRequests.stream()
                .filter(m -> applyFilters(m, filter))
                .sorted(Comparator.comparing(CollaborationMatchRequest::getRequestedAt).reversed())
                .map(this::toManagementDto)
                .collect(Collectors.toList());
    }

    /**
     * 필터 적용
     */
    private boolean applyFilters(
            CollaborationMatchRequest match,
            StudentOrgMatchingStatisticsDto.StudentOrgMatchingFilterRequest filter
    ) {
        // 기업명 필터
        if (filter.getCompanyName() != null && !filter.getCompanyName().isEmpty()) {
            String companyName = match.getCompany() != null ? match.getCompany().getBrandName() : "";
            if (!companyName.contains(filter.getCompanyName())) {
                return false;
            }
        }

        // 협업 형태 필터
        if (filter.getCollaborationType() != null) {
            if (match.getCollaborationType() != filter.getCollaborationType()) {
                return false;
            }
        }

        // 매칭 상태 필터
        if (filter.getMatchingStatus() != null && !filter.getMatchingStatus().equals("ALL")) {
            if (!match.getStatus().name().equals(filter.getMatchingStatus())) {
                return false;
            }
        }

        // 캠페인 시작일 필터
        LocalDate campaignStartDate = getCampaignStartDate(match);
        if (campaignStartDate != null) {
            if (filter.getCampaignStartDateFrom() != null
                    && campaignStartDate.isBefore(filter.getCampaignStartDateFrom())) {
                return false;
            }
            if (filter.getCampaignStartDateTo() != null
                    && campaignStartDate.isAfter(filter.getCampaignStartDateTo())) {
                return false;
            }
        }

        // 매칭 요청일 필터
        if (match.getRequestedAt() != null) {
            LocalDate requestDate = match.getRequestedAt().toLocalDate();
            if (filter.getMatchingRequestDateFrom() != null
                    && requestDate.isBefore(filter.getMatchingRequestDateFrom())) {
                return false;
            }
            if (filter.getMatchingRequestDateTo() != null
                    && requestDate.isAfter(filter.getMatchingRequestDateTo())) {
                return false;
            }
        }

        // 프로세스 상태 필터
        if (filter.getProcessStatus() != null && !filter.getProcessStatus().isEmpty()) {
            String processStatus = getProcessStatus(match);
            if (!processStatus.equals(filter.getProcessStatus())) {
                return false;
            }
        }

        return true;
    }

    /**
     * DTO 변환
     */
    private StudentOrgMatchingStatisticsDto.StudentOrgMatchingManagementDto toManagementDto(CollaborationMatchRequest match) {
        String contractStatus = getContractStatus(match);
        String processStatus = getProcessStatus(match);
        LocalDate campaignStartDate = getCampaignStartDate(match);
        LocalDate campaignEndDate = getCampaignEndDate(match);
        String companyName = match.getCompany() != null ? match.getCompany().getBrandName() : "N/A";
        String campaignName = getCampaignName(match);

        return StudentOrgMatchingStatisticsDto.StudentOrgMatchingManagementDto.builder()
                .matchRequestId(match.getId())
                .companyName(companyName)
                .campaignStartDate(campaignStartDate)
                .campaignEndDate(campaignEndDate)
                .requestedAt(match.getRequestedAt())
                .collaborationType(match.getCollaborationType())
                .status(match.getStatus())
                .sender(match.getSender())
                .contractStatus(contractStatus)
                .processStatus(processStatus)
                .collaborationId(getCollaborationId(match))
                .campaignName(campaignName)
                .build();
    }

    /**
     * 계약 상태 조회
     */
    private String getContractStatus(CollaborationMatchRequest match) {
        return collaborationRepository.findByMatchRequest(match)
                .flatMap(contractRepository::findByCollaboration)
                .map(contract -> contract.getStatus() == ContractStatus.Signed
                        || contract.getStatus() == ContractStatus.ReceiptSigned
                        ? "계약 완료"
                        : "서명 전")
                .orElse("계약 전");
    }

    /**
     * 프로세스 상태 조회
     */
    private String getProcessStatus(CollaborationMatchRequest match) {
        Optional<Collaboration> collaboration = collaborationRepository.findByMatchRequest(match);

        if (collaboration.isEmpty()) {
            return "계약 전";
        }

        return switch (collaboration.get().getStatus().name()) {
            case "ContractSent" -> "계약서 작성 중";
            case "WaitingStudentSignature" -> "계약서 작성 중";
            case "WaitingAdminApproval" -> "계약 완료";
            case "WaitingReportUpload" -> "설문지 관리";
            case "WaitingReportApproval" -> "데이터 리포트";
            case "Completed" -> "협업 종료";
            default -> "계약 전";
        };
    }

    /**
     * 캠페인 시작일 조회
     */
    private LocalDate getCampaignStartDate(CollaborationMatchRequest match) {
        if (match.getCampaign() != null) {
            return match.getCampaign().getStartDate();
        }
        if (match.getSamplingProposal() != null) {
            return match.getSamplingProposal().getSamplingStartDate();
        }
        if (match.getCollaborationProposal() != null) {
            return match.getCollaborationProposal().getStartDate();
        }
        return null;
    }

    /**
     * 캠페인 종료일 조회
     */
    private LocalDate getCampaignEndDate(CollaborationMatchRequest match) {
        if (match.getCampaign() != null) {
            return match.getCampaign().getEndDate();
        }
        if (match.getSamplingProposal() != null) {
            return match.getSamplingProposal().getSamplingEndDate();
        }
        if (match.getCollaborationProposal() != null) {
            return match.getCollaborationProposal().getEndDate();
        }
        return null;
    }

    /**
     * 캠페인명 조회
     */
    private String getCampaignName(CollaborationMatchRequest match) {
        if (match.getCampaign() != null) {
            return match.getCampaign().getName();
        }
        if (match.getSamplingProposal() != null) {
            return match.getSamplingProposal().getProductName();
        }
        if (match.getCollaborationProposal() != null) {
            return match.getCollaborationProposal().getProductOrServiceName();
        }
        return "N/A";
    }

    /**
     * Collaboration ID 조회
     */
    private Long getCollaborationId(CollaborationMatchRequest match) {
        return collaborationRepository.findByMatchRequest(match)
                .map(Collaboration::getId)
                .orElse(null);
    }

    /**
     * 로그인한 학생단체 조회
     */
    private StudentOrg getStudentOrgFromLogin() {
        CustomUser principal = (CustomUser)
                SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = Long.valueOf(principal.getUserId());

        return studentOrgRepository.findByUsers_UserId(userId)
                .orElseThrow(() -> new IllegalStateException("소속 학생단체가 없습니다."));
    }

    /**
     * 매칭 상태 승인
     */
    @Transactional
    public void approveMatching(Long matchRequestId) {
        CollaborationMatchRequest match = matchRequestRepository.findById(matchRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Matching not found"));

        match.approve();
        matchRequestRepository.save(match);

        // 통계 업데이트
        updateStatistics(match.getStudentOrg().getStudentOrgId(), "Approved");
    }

    /**
     * 매칭 상태 거절
     */
    @Transactional
    public void rejectMatching(Long matchRequestId) {
        CollaborationMatchRequest match = matchRequestRepository.findById(matchRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Matching not found"));

        match.reject();
        matchRequestRepository.save(match);

        // 통계 업데이트
        updateStatistics(match.getStudentOrg().getStudentOrgId(), "Rejected");
    }

    /**
     * 통계 업데이트
     */
    @Transactional
    public void updateStatistics(Long studentOrgId, String status) {
        StudentOrgMatchingStatistics stats = statisticsRepository.findByStudentOrgId(studentOrgId)
                .orElse(StudentOrgMatchingStatistics.builder()
                        .studentOrgId(studentOrgId)
                        .totalMatchings(0)
                        .completedMatchings(0)
                        .pendingMatchings(0)
                        .failedMatchings(0)
                        .build());

        stats.incrementTotal();
        stats.updateStatus("Requested", status);

        statisticsRepository.save(stats);
    }
}
