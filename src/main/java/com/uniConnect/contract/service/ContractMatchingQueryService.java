package com.uniConnect.contract.service;

import com.uniConnect.contract.dto.MyMatchingListItemDto;
import com.uniConnect.contract.dto.MatchingFilterRequest;
import com.uniConnect.contract.entity.Contract;
import com.uniConnect.contract.enums.ContractStatus;
import com.uniConnect.contract.repository.ContractRepository;
import com.uniConnect.collaboration.repository.CollaborationRepository;
import com.uniConnect.company.entity.Company;
import com.uniConnect.company.repository.CompanyRepository;
import com.uniConnect.matching.entity.CollaborationMatchRequest;
import com.uniConnect.matching.repository.CollaborationMatchRequestRepository;
import com.uniConnect.campaign.enums.MatchingStatus;
import com.uniConnect.member.security.local.CustomUser;
import com.uniConnect.partnership.enums.PartnershipType;
import com.uniConnect.studentOrg.entity.StudentOrg;
import com.uniConnect.studentOrg.enums.CollaborationType;
import com.uniConnect.studentOrg.repository.StudentOrgRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContractMatchingQueryService {

    private final CollaborationMatchRequestRepository matchRequestRepository;
    private final CollaborationRepository collaborationRepository;
    private final ContractRepository contractRepository;
    private final StudentOrgRepository studentOrgRepository;
    private final CompanyRepository companyRepository;

    /* =========================
       학생단체용 매칭 조회
    ========================= */
    public List<MyMatchingListItemDto> getMyMatchingList() {

        StudentOrg studentOrg = getStudentOrgFromLogin();

        return matchRequestRepository
                .findByStudentOrg_StudentOrgIdAndStatus(
                        studentOrg.getStudentOrgId(),
                        MatchingStatus.Approved
                )
                .stream()
                .sorted(Comparator.comparing(
                        CollaborationMatchRequest::getRespondedAt
                ).reversed())
                .map(this::toDto)
                .toList();
    }

    /* =========================
       기업용 매칭 조회
    ========================= */
    public List<MyMatchingListItemDto> getMyMatchingsForCompany() {

        Company company = getCompanyFromLogin();

        return matchRequestRepository
                .findByCompany_CompanyIdAndStatus(
                        company.getCompanyId(),
                        MatchingStatus.Approved
                )
                .stream()
                .sorted(Comparator.comparing(
                        CollaborationMatchRequest::getRespondedAt
                ).reversed())
                .map(this::toDto)
                .toList();
    }

    /* =========================
       기업용 필터 조회
    ========================= */
    public List<MyMatchingListItemDto> getCompanyMatchingsWithFilter(
            MatchingFilterRequest filter
    ) {
        return getMyMatchingsForCompanyRaw()
                .stream()
                .filter(m -> filterByName(m, filter))
                .filter(m -> filterByPeriod(m, filter))
                .filter(m -> filterByDate(m, filter))
                .filter(m -> filterByCollaborationType(m, filter))
                .filter(m -> filterByContractStatus(m, filter))
                .sorted(Comparator.comparing(
                        CollaborationMatchRequest::getRespondedAt
                ).reversed())
                .map(this::toDto)
                .toList();
    }

    /* =========================
       학생단체용 필터 조회
    ========================= */
    public List<MyMatchingListItemDto> getStudentOrgMatchingsWithFilter(
            MatchingFilterRequest filter
    ) {
        return getMyMatchingsForStudentOrgRaw()
                .stream()
                .filter(m -> filterByName(m, filter))
                .filter(m -> filterByPeriod(m, filter))
                .filter(m -> filterByDate(m, filter))
                .filter(m -> filterByCollaborationType(m, filter))
                .filter(m -> filterByContractStatus(m, filter))
                .sorted(Comparator.comparing(
                        CollaborationMatchRequest::getRespondedAt
                ).reversed())
                .map(this::toDto)
                .toList();
    }

    /* =========================
       Raw 조회
    ========================= */
    private List<CollaborationMatchRequest> getMyMatchingsForCompanyRaw() {
        Company company = getCompanyFromLogin();
        return matchRequestRepository.findByCompany_CompanyIdAndStatus(
                company.getCompanyId(),
                MatchingStatus.Approved
        );
    }

    private List<CollaborationMatchRequest> getMyMatchingsForStudentOrgRaw() {
        StudentOrg org = getStudentOrgFromLogin();
        return matchRequestRepository.findByStudentOrg_StudentOrgIdAndStatus(
                org.getStudentOrgId(),
                MatchingStatus.Approved
        );
    }

    /* =========================
       DTO 변환
    ========================= */
    private MyMatchingListItemDto toDto(CollaborationMatchRequest match) {

        ContractStatus contractStatus =
                collaborationRepository.findByMatchRequest(match)
                        .flatMap(contractRepository::findByCollaboration)
                        .map(Contract::getStatus)
                        .orElse(ContractStatus.PendingSignature);

        // 매칭 상대 회사명 안전하게 조회
        Company company = resolveCompany(match);
        String companyName = company != null ? company.getBrandName() : null;


        return MyMatchingListItemDto.of(
                match.getId(),
                match.getRespondedAt(),
                match.getStudentOrg().getOrganizationName(),
                match.getCollaborationType(),
                contractStatus,
                match.getCompany().getBrandName()
        );
    }

    /* =========================
       필터 로직
    ========================= */
    private boolean matchPeriod(LocalDate s, LocalDate e, String period) {
        if (s == null || e == null) return false;
        long days = ChronoUnit.DAYS.between(s, e);

        return switch (period) {
            case "1M" -> days <= 31;
            case "3M" -> days <= 92;
            case "6M" -> days <= 183;
            case "ALWAYS" -> true;
            default -> true;
        };
    }

    private boolean filterByName(CollaborationMatchRequest m, MatchingFilterRequest f) {
        if (f.getKeyword() == null || f.getKeyword().isBlank()) return true;

        String keyword = f.getKeyword().trim();

        String studentOrgName =
                m.getStudentOrg() != null
                        ? m.getStudentOrg().getOrganizationName()
                        : "";

        // 회사명도 resolveCompany()로 일관되게 조회
        Company company = resolveCompany(m);
        String companyName =
                company != null
                        ? company.getBrandName()
                        : "";

        return studentOrgName.contains(keyword)
                || companyName.contains(keyword);
    }

    private boolean filterByPeriod(CollaborationMatchRequest m, MatchingFilterRequest f) {
        if (f.getPeriod() == null) return true;

        if (m.getSamplingProposal() != null) {
            return matchPeriod(
                    m.getSamplingProposal().getSamplingStartDate(),
                    m.getSamplingProposal().getSamplingEndDate(),
                    f.getPeriod()
            );
        }

        if (m.getCollaborationProposal() != null) {
            return m.getCollaborationProposal()
                    .getPeriodType()
                    .name()
                    .equalsIgnoreCase(f.getPeriod());
        }

        return true;
    }

    private boolean filterByDate(CollaborationMatchRequest m, MatchingFilterRequest f) {
        if (f.getStartDate() == null && f.getEndDate() == null) return true;

        LocalDate s = null;
        LocalDate e = null;

        if (m.getSamplingProposal() != null) {
            s = m.getSamplingProposal().getSamplingStartDate();
            e = m.getSamplingProposal().getSamplingEndDate();
        } else if (m.getCollaborationProposal() != null) {
            s = m.getCollaborationProposal().getStartDate();
            e = m.getCollaborationProposal().getEndDate();
        }

        if (s == null || e == null) return false;
        if (f.getStartDate() != null && e.isBefore(f.getStartDate())) return false;
        if (f.getEndDate() != null && s.isAfter(f.getEndDate())) return false;

        return true;
    }

    private boolean filterByCollaborationType(CollaborationMatchRequest m, MatchingFilterRequest f) {
        if (f.getCollaborationType() == null) return true;

        return switch (f.getCollaborationType()) {
            case "SAMPLING" ->
                    m.getCollaborationType() == CollaborationType.Sampling;
            case "PARTNERSHIP" ->
                    m.getCollaborationType() == CollaborationType.Partnership;
            case "DISCOUNT" ->
                    m.getCollaborationType() == CollaborationType.Partnership &&
                            m.getCollaborationProposal() != null &&
                            m.getCollaborationProposal().getProposalType() == PartnershipType.Discount;
            case "ETC" ->
                    m.getCollaborationType() == CollaborationType.Partnership &&
                            m.getCollaborationProposal() != null &&
                            m.getCollaborationProposal().getProposalType() == PartnershipType.Etc;
            default -> true;
        };
    }

    private boolean filterByContractStatus(CollaborationMatchRequest m, MatchingFilterRequest f) {
        if (f.getContractStatus() == null) return true;

        Optional<Contract> c =
                collaborationRepository.findByMatchRequest(m)
                        .flatMap(contractRepository::findByCollaboration);

        return switch (f.getContractStatus()) {
            case "NEED_SEND" -> c.isEmpty();
            case "BEFORE_SIGN" ->
                    c.isPresent() &&
                            c.get().getStatus() != ContractStatus.Signed &&
                            c.get().getStatus() != ContractStatus.ReceiptSigned;
            case "COMPLETED" ->
                    c.isPresent() &&
                            (c.get().getStatus() == ContractStatus.Signed ||
                                    c.get().getStatus() == ContractStatus.ReceiptSigned);
            default -> true;
        };
    }

    /* =========================
       로그인 유틸
    ========================= */
    private StudentOrg getStudentOrgFromLogin() {
        CustomUser principal = (CustomUser)
                SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = Long.valueOf(principal.getUserId());

        return studentOrgRepository.findByUsers_UserId(userId)
                .orElseThrow(() -> new IllegalStateException("소속 학생단체가 없습니다."));
    }

    private Company getCompanyFromLogin() {
        CustomUser principal = (CustomUser)
                SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = Long.valueOf(principal.getUserId());

        return companyRepository.findByUsers_UserId(userId)
                .orElseThrow(() -> new IllegalStateException("소속 기업이 없습니다."));
    }

    private Company resolveCompany(CollaborationMatchRequest match) {
        // CollaborationMatchRequest.getCompany()는
        // - 직접 company
        // - samplingProposal.creator.company
        // - collaborationProposal.company
        // 까지 처리해주므로 우선 사용
        Company company = match.getCompany();
        if (company != null) {
            return company;
        }

        // 필요 시 campaign 쪽에 company가 연결돼 있다면 여기에서 추가로 처리 가능
        // (현재 도메인 구조에 따라 선택)
        // if (match.getCampaign() != null && match.getCampaign().getCompany() != null) {
        //     return match.getCampaign().getCompany();
        // }

        return null;
    }
}
