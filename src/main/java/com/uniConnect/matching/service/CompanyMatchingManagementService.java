package com.uniConnect.matching.service;

import com.uniConnect.campaign.enums.MatchingStatus;
import com.uniConnect.collaboration.entity.Collaboration;
import com.uniConnect.collaboration.repository.CollaborationRepository;
import com.uniConnect.company.entity.Company;
import com.uniConnect.contract.enums.ContractStatus;
import com.uniConnect.contract.repository.ContractRepository;
import com.uniConnect.matching.dto.CompanyMatchingStatisticsDto;
import com.uniConnect.matching.entity.CollaborationMatchRequest;
import com.uniConnect.matching.entity.CompanyMatchingStatistics;
import com.uniConnect.matching.enums.MatchSender;
import com.uniConnect.matching.repository.CollaborationMatchRequestRepository;
import com.uniConnect.matching.repository.CompanyMatchingStatisticsRepository;
import com.uniConnect.company.repository.CompanyRepository;
import com.uniConnect.member.security.local.CustomUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyMatchingManagementService {

    private final CollaborationMatchRequestRepository matchRequestRepository;
    private final CompanyMatchingStatisticsRepository statisticsRepository;
    private final CollaborationRepository collaborationRepository;
    private final ContractRepository contractRepository;
    private final CompanyRepository companyRepository;

    /**
     * 기업의 전체 매칭 관리 페이지 조회
     */
    public CompanyMatchingStatisticsDto.CompanyMatchingPageResponse getMatchingManagementPage(
            CompanyMatchingStatisticsDto.CompanyMatchingFilterRequest filter,
            Pageable pageable
    ) {
        Company company = getCompanyFromLogin();

        // 1. 통계 조회
        CompanyMatchingStatisticsDto.CompanyMatchingStatisticsResponse statistics = getMatchingStatistics(company.getCompanyId());

        // 2. 요청한 매칭 조회 (COMPANY 기준)
        List<CompanyMatchingStatisticsDto.CompanyMatchingManagementDto> sentMatchings =
                getSentMatchings(company, filter);

        // 3. 받은 요청 조회 (STUDENT_ORG 기준)
        List<CompanyMatchingStatisticsDto.CompanyMatchingManagementDto> receivedMatchings =
                getReceivedMatchings(company, filter);

        // 페이징 처리
        long totalElements = sentMatchings.size() + receivedMatchings.size();
        int start = (int) pageable.getOffset();
        int end = Math.min((int) (start + pageable.getPageSize()), (int) totalElements);

        List<CompanyMatchingStatisticsDto.CompanyMatchingManagementDto> paginatedList = new ArrayList<>();
        if (start < sentMatchings.size()) {
            paginatedList.addAll(sentMatchings.subList(start, Math.min(end, sentMatchings.size())));
        }
        if (end > sentMatchings.size() && paginatedList.size() < pageable.getPageSize()) {
            int receivedStart = Math.max(0, start - sentMatchings.size());
            int receivedEnd = Math.min(end - sentMatchings.size(), receivedMatchings.size());
            paginatedList.addAll(receivedMatchings.subList(receivedStart, receivedEnd));
        }

        return CompanyMatchingStatisticsDto.CompanyMatchingPageResponse.builder()
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
    public CompanyMatchingStatisticsDto.CompanyMatchingStatisticsResponse getMatchingStatistics(Long companyId) {
        // 모든 요청한 매칭
        Integer total = matchRequestRepository.findAllCompanyMatchingsBySender(
                companyId,
                MatchSender.COMPANY
        ).size();

        // 완료된 매칭
        Integer completed = matchRequestRepository.findAllCompanyMatchingsBySenderAndStatus(
                companyId,
                MatchSender.COMPANY,
                MatchingStatus.Approved
        ).size();

        // 대기 중
        Integer pending = matchRequestRepository.findAllCompanyMatchingsBySenderAndStatus(
                companyId,
                MatchSender.COMPANY,
                MatchingStatus.Requested
        ).size();

        // 실패
        Integer failed = matchRequestRepository.findAllCompanyMatchingsBySenderAndStatus(
                companyId,
                MatchSender.COMPANY,
                MatchingStatus.Rejected
        ).size();

        return CompanyMatchingStatisticsDto.CompanyMatchingStatisticsResponse.builder()
                .totalCount(total)
                .completedCount(completed)
                .pendingCount(pending)
                .failedCount(failed)
                .build();

    }

    /**
     * 기업이 요청한 매칭 조회
     */
    private List<CompanyMatchingStatisticsDto.CompanyMatchingManagementDto> getSentMatchings(
            Company company,
            CompanyMatchingStatisticsDto.CompanyMatchingFilterRequest filter
    ) {
        List<CollaborationMatchRequest> matchRequests =
                matchRequestRepository.findByCompany_CompanyIdAndSender(
                        company.getCompanyId(),
                        MatchSender.COMPANY
                );

        return matchRequests.stream()
                .filter(m -> applyFilters(m, filter))
                .sorted(Comparator.comparing(CollaborationMatchRequest::getRequestedAt).reversed())
                .map(this::toManagementDto)
                .collect(Collectors.toList());
    }

    /**
     * 기업이 받은 요청 조회
     */
    private List<CompanyMatchingStatisticsDto.CompanyMatchingManagementDto> getReceivedMatchings(
            Company company,
            CompanyMatchingStatisticsDto.CompanyMatchingFilterRequest filter
    ) {
        List<CollaborationMatchRequest> matchRequests =
                matchRequestRepository.findByCompany_CompanyIdAndSender(
                        company.getCompanyId(),
                        MatchSender.STUDENT_ORG
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
            CompanyMatchingStatisticsDto.CompanyMatchingFilterRequest filter
    ) {
        // 학생 단체명 필터
        if (filter.getStudentOrgName() != null && !filter.getStudentOrgName().isEmpty()) {
            String orgName = match.getStudentOrg() != null ? match.getStudentOrg().getOrganizationName() : "";
            if (!orgName.contains(filter.getStudentOrgName())) {
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

        // 희망 협업일 필터
        LocalDate desiredDate = getDesiredCollaborationDate(match);
        if (desiredDate != null) {
            if (filter.getDesiredCollaborationDateFrom() != null
                    && desiredDate.isBefore(filter.getDesiredCollaborationDateFrom())) {
                return false;
            }
            if (filter.getDesiredCollaborationDateTo() != null
                    && desiredDate.isAfter(filter.getDesiredCollaborationDateTo())) {
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
    private CompanyMatchingStatisticsDto.CompanyMatchingManagementDto toManagementDto(CollaborationMatchRequest match) {
        String contractStatus = getContractStatus(match);
        String processStatus = getProcessStatus(match);
        LocalDate desiredDate = getDesiredCollaborationDate(match);

        return CompanyMatchingStatisticsDto.CompanyMatchingManagementDto.builder()
                .matchRequestId(match.getId())
                .studentOrgName(match.getStudentOrg() != null ? match.getStudentOrg().getOrganizationName() : "N/A")
                .desiredCollaborationDate(desiredDate)
                .requestedAt(match.getRequestedAt())
                .collaborationType(match.getCollaborationType())
                .status(match.getStatus())
                .sender(match.getSender())
                .contractStatus(contractStatus)
                .processStatus(processStatus)
                .collaborationId(getCollaborationId(match))
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
     * 희망 협업일 조회
     */
    private LocalDate getDesiredCollaborationDate(CollaborationMatchRequest match) {
        if (match.getSamplingProposal() != null) {
            return match.getSamplingProposal().getSamplingStartDate();
        }
        if (match.getCollaborationProposal() != null) {
            return match.getCollaborationProposal().getStartDate();
        }
        if (match.getCampaign() != null) {
            return match.getCampaign().getStartDate();
        }
        return null;
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
     * 로그인한 기업 조회
     */
    private Company getCompanyFromLogin() {
        CustomUser principal = (CustomUser)
                SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = Long.valueOf(principal.getUserId());

        return companyRepository.findByUsers_UserId(userId)
                .orElseThrow(() -> new IllegalStateException("소속 기업이 없습니다."));
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
        updateStatistics(match.getCompany().getCompanyId(), "Approved");
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
        updateStatistics(match.getCompany().getCompanyId(), "Rejected");
    }

    /**
     * 통계 업데이트
     */
    @Transactional
    public void updateStatistics(Long companyId, String status) {
        CompanyMatchingStatistics stats = statisticsRepository.findByCompanyId(companyId)
                .orElse(CompanyMatchingStatistics.builder()
                        .companyId(companyId)
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