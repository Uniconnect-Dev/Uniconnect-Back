package com.uniConnect.sampling.service;

import com.uniConnect.studentOrg.enums.CollaborationType;
import com.uniConnect.studentOrg.entity.StudentOrg;
import com.uniConnect.sampling.enums.IndustryType;
import com.uniConnect.member.entity.User;
import com.uniConnect.company.repository.CompanyRepository;
import com.uniConnect.company.entity.Company;
import com.uniConnect.member.repository.UserRepository;
import com.uniConnect.matching.entity.CollaborationMatchRequest;
import com.uniConnect.campaign.enums.MatchingStatus;
import com.uniConnect.matching.repository.CollaborationMatchRequestRepository;
import com.uniConnect.s3.S3FileService;
import com.uniConnect.sampling.dto.request.*;
import com.uniConnect.sampling.dto.response.*;
import com.uniConnect.sampling.dto.FeeResponse;
import com.uniConnect.sampling.entity.*;
import com.uniConnect.sampling.enums.*;
import com.uniConnect.sampling.enums.SamplingStatus;
import com.uniConnect.sampling.repository.*;
import com.uniConnect.global.exception.CustomException;
import com.uniConnect.global.exception.ErrorCode;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Transactional
public class SamplingRequestService {

    private final SamplingRequestRepository requestRepository;
    private final SamplingTargetKeywordRepository keywordRepository;
    private final SamplingTargetSelectionRepository selectionRepository;
    private final S3FileService s3FileService;
    private final SamplingRequestRepository samplingRequestRepository;
    private final UserRepository userRepository;
    private final CollaborationMatchRequestRepository collaborationMatchRequestRepository;
    private final SamplingSelectedCompanyRepository selectedCompanyRepository;
    private final CompanyRepository companyRepository;

    private static final int PER_SAMPLING_FEE = 50_000;   // 건당 수수료
    private static final int DEPOSIT = 50_000;            // 보증금

    @Value("${app.s3.bucket}")
    private String bucketName;

    public Long getOwnerId(Long id) {
        SamplingRequest req = samplingRequestRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.SAMPLING_REQUEST_NOT_FOUND));

        return req.getUser().getUserId();
    }

    /** Step 0: 샘플링 요청 초안 생성 */
    public SamplingRequest createDraft(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        StudentOrg org = user.getStudentOrg();
        if (org == null) {
            throw new IllegalStateException("해당 유저는 학생단체 정보가 없습니다.");
        }

        SamplingRequest draft = SamplingRequest.builder()
                .user(user)
                .status(SamplingStatus.Draft)
                .schoolName(org.getSchoolName())
                .orgName(org.getOrganizationName())
                .contactName(org.getManagerName())
                .phone(org.getPhone())
                .email(org.getEmail())

                .build();

        return samplingRequestRepository.save(draft);
    }



    /** Step 1: 단체 기본 정보 입력 */
    public void updateStep1(Long id, OrgInfoRequest dto) {
        SamplingRequest req = findById(id);
        req.setSchoolName(dto.schoolName());
        req.setOrgName(dto.orgName());
        req.setContactName(dto.contactName());
        req.setPhone(dto.phone());
        req.setEmail(dto.email());
    }

    /** Step 2: 단체 해시태그 선택 (BasicInfo, Lifestyle) */
    public void updateStep2(Long id, TargetTagRequest dto) {
        SamplingRequest req = findById(id);

        if (dto.basicInfoTagIds() == null || dto.basicInfoTagIds().isEmpty()) {
            throw new IllegalArgumentException("기본 정보 해시태그는 최소 1개 이상 선택해야 합니다.");
        }
        if (dto.lifestyleTagIds() == null || dto.lifestyleTagIds().isEmpty()) {
            throw new IllegalArgumentException("라이프스타일 해시태그는 최소 1개 이상 선택해야 합니다.");
        }
        if (dto.basicInfoTagIds().size() > 5 || dto.lifestyleTagIds().size() > 5) {
            throw new IllegalArgumentException("각 카테고리별 최대 5개까지만 선택 가능합니다.");
        }

        List<SamplingTargetSelection> toRemove = req.getSelections().stream()
                .filter(s ->
                        s.getCategory() == SamplingTargetCategory.BasicInfo ||
                                s.getCategory() == SamplingTargetCategory.Lifestyle
                )
                .toList();
        selectionRepository.deleteAll(toRemove);

        // ===== 새 태그 저장 =====
        List<Long> allIds = new ArrayList<>();
        allIds.addAll(dto.basicInfoTagIds());
        allIds.addAll(dto.lifestyleTagIds());

        List<SamplingTargetKeyword> tags = keywordRepository.findAllById(allIds);

        for (SamplingTargetKeyword tag : tags) {
            if (tag.getCategory() == SamplingTargetCategory.BasicInfo ||
                    tag.getCategory() == SamplingTargetCategory.Lifestyle) {
                req.addSelection(SamplingTargetSelection.builder()
                        .samplingRequest(req)
                        .targetKeyword(tag)
                        .category(tag.getCategory())
                        .selectedLabel(tag.getLabel())
                        .build());
            }
        }
    }

    /** Step 3: 행사 정보 입력 (EventNature) */
    public void updateStep3(Long id, EventInfoRequest dto) {
        SamplingRequest req = findById(id);

        if (dto.eventTitle() == null || dto.eventTitle().isBlank()) {
            throw new IllegalArgumentException("행사명은 필수 입력 항목입니다.");
        }
        if (dto.eventDescription() != null && dto.eventDescription().length() > 500) {
            throw new IllegalArgumentException("행사 설명은 500자 이내여야 합니다.");
        }

        req.setEventTitle(dto.eventTitle());
        req.setEventDescription(dto.eventDescription());
        req.setRequestedQuantity(dto.requestedQuantity());
        req.setEventStartDate(dto.eventStartDate());
        req.setEventEndDate(dto.eventEndDate());

        List<SamplingTargetSelection> toRemove = req.getSelections().stream()
                .filter(s -> s.getCategory() == SamplingTargetCategory.EventNature)
                .toList();
        selectionRepository.deleteAll(toRemove);

        var tags = keywordRepository.findAllById(dto.eventTagIds());
        for (SamplingTargetKeyword tag : tags) {
            if (tag.getCategory() == SamplingTargetCategory.EventNature) {
                req.addSelection(SamplingTargetSelection.builder()
                        .samplingRequest(req)
                        .targetKeyword(tag)
                        .category(tag.getCategory())
                        .selectedLabel(tag.getLabel())
                        .build());
            }
        }
    }


    /** Step 4: 산업군 및 공식 해시태그 선택 */
    public void updateStep4(Long id, IndustryRequest dto) {
        SamplingRequest req = findById(id);

        if (dto.detailRequest() != null && dto.detailRequest().length() > 250) {
            throw new IllegalArgumentException("세부 요청 사항은 250자 이내여야 합니다.");
        }

        req.setIndustry(IndustryType.valueOf(dto.industry()));
        req.setDetailRequest(dto.detailRequest());

        List<SamplingTargetSelection> toRemove = req.getSelections().stream()
                .filter(s -> s.getCategory() == SamplingTargetCategory.IndustryOfficial)
                .toList();
        selectionRepository.deleteAll(toRemove);

        var tags = keywordRepository.findAllById(dto.industryTagIds());
        for (SamplingTargetKeyword tag : tags) {
            if (tag.getCategory() == SamplingTargetCategory.IndustryOfficial) {
                req.addSelection(SamplingTargetSelection.builder()
                        .samplingRequest(req)
                        .targetKeyword(tag)
                        .category(tag.getCategory())
                        .selectedLabel(tag.getLabel())
                        .build());
            }
        }
    }


    /** Step 5: 제안서 업로드 (S3) */
    public String uploadProposal(Long id, MultipartFile file) throws Exception {
        SamplingRequest req = findById(id);
        String key = "proposals/" + id + "_" + file.getOriginalFilename();
        s3FileService.upload(bucketName, key, file);
        String url = "https://" + bucketName + ".s3.ap-northeast-2.amazonaws.com/" + key;
        req.setProposalFileUrl(url);
        req.setStatus(SamplingStatus.Submitted);
        return url;
    }

    public SamplingRequestSummaryResponse saveSelectedCompanies(Long id, List<Long> companyIds) {

        if (companyIds == null || companyIds.isEmpty())
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);

        if (companyIds.size() > 5)
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);

        SamplingRequest req = findById(id);

        selectedCompanyRepository.deleteBySamplingRequest_SamplingRequestId(id);
        req.getSelectedCompanies().clear();

        for (Long cid : companyIds) {

            Company company = companyRepository.findById(cid)
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

            req.addSelectedCompany(SamplingSelectedCompany.of(req, company));
        }

        return getSummary(id);
    }

    public int calcCost(Long id) {
        SamplingRequest req = findById(id);
        return req.getSelectedCompanies().size() * PER_SAMPLING_FEE;
    }


    /** 관리자 승인/반려 */
    public void approveRequest(Long id) {
        SamplingRequest req = findById(id);
        req.setStatus(SamplingStatus.Approved);
    }

    public void rejectRequest(Long id) {
        SamplingRequest req = findById(id);
        req.setStatus(SamplingStatus.Rejected);
    }

    /** Step 1: 계약서 서명 → 승인 대기 */
    public void signContract(Long id, Long userId) {

        SamplingRequest req = samplingRequestRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.REQUEST_NOT_FOUND));

        if (!req.getUser().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        req.setStatus(SamplingStatus.ContractApprovalPending);

        samplingRequestRepository.save(req);
    }

    /** Step 2: 계약 승인 후 → 인수증 대기 */
    public void approveContract(Long id) {
        SamplingRequest req = findById(id);
        req.setStatus(SamplingStatus.ReceiptPending);
    }

    /** Step 3: 인수증 업로드 → 승인 대기 */
    public void uploadReceipt(Long id, MultipartFile file) throws Exception {
        SamplingRequest req = findById(id);
        String key = "receipts/" + id + "_" + file.getOriginalFilename();
        s3FileService.upload(bucketName, key, file);
        req.setStatus(SamplingStatus.ReceiptApprovalPending);
    }

    /** Step 4: 인수증 승인 → 리포트 대기 */
    public void approveReceipt(Long id) {
        SamplingRequest req = findById(id);
        req.setStatus(SamplingStatus.ReportPending);
    }

    /** Step 5: 리포트 업로드 → 승인 대기 */
    public void uploadReport(Long id, MultipartFile file) throws Exception {
        SamplingRequest req = findById(id);
        String key = "reports/" + id + "_" + file.getOriginalFilename();
        s3FileService.upload(bucketName, key, file);
        req.setStatus(SamplingStatus.ReportApprovalPending);
    }

    /** Step 6: 리포트 승인 → 설문 대기 */
    public void approveReport(Long id) {
        SamplingRequest req = findById(id);
        req.setStatus(SamplingStatus.SurveyPending);
    }

    /** Step 7: 설문 완료 → 전체 완료 */
    public void completeSurvey(Long id) {
        SamplingRequest req = findById(id);
        req.setStatus(SamplingStatus.Completed);
    }

    /** Step 6: 최종 제출 */
    public void submit(Long id) {

        SamplingRequest req = findById(id);

        if (req.getSelectedCompanies().isEmpty())
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);

        Long studentOrgId = req.getUser().getStudentOrg().getStudentOrgId();

//        for (SamplingSelectedCompany sc : req.getSelectedCompanies()) {
//
//            CollaborationMatchRequest match = CollaborationMatchRequest.builder()
//                    .studentOrgId(studentOrgId)
//                    .companyId(sc.getCompany().getCompanyId())
//                    .eventTitle(req.getEventTitle())
//                    .desiredDate(req.getEventStartDate())
//                    .industry(req.getIndustry())
//                    .collaborationType(CollaborationType.Sampling.name())
//                    .status(MatchingStatus.Requested)
//                    .requestedAt(LocalDateTime.now())
//                    .build();
//
//            collaborationMatchRequestRepository.save(match);
//        }

        req.setStatus(SamplingStatus.Submitted);
    }

    /** 요약 조회 */
    public SamplingRequestSummaryResponse getSummary(Long id) {
        SamplingRequest req = findById(id);

        List<String> tagList = req.getSelections().stream()
                .map(s -> s.getTargetKeyword().getLabel())
                .collect(Collectors.toList());

        return new SamplingRequestSummaryResponse(
                req.getSamplingRequestId(),
                req.getSchoolName(),
                req.getOrgName(),
                req.getContactName(),
                req.getPhone(),
                req.getEmail(),
                req.getEventTitle(),
                req.getEventDescription(),
                req.getRequestedQuantity(),
                req.getEventStartDate(),
                req.getEventEndDate(),
                req.getIndustry(),
                req.getProposalFileUrl(),
                req.getStatus(),
                tagList
        );
    }

    /** 학생단체 금액 안내 */
    public FeeResponse getFeeInformation() {
        return FeeResponse.builder()
                .perSamplingFee(PER_SAMPLING_FEE)
                .deposit(DEPOSIT)
                .totalEstimatedAmount(PER_SAMPLING_FEE + DEPOSIT)
                .build();
    }

    public SamplingRequest findById(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("요청을 찾을 수 없습니다."));
    }

    /** 학생단체가 기업 매칭 요청을 승인(완료) */
    public void requestMatching(Long id) {
        SamplingRequest req = findById(id);

        if (req.getStatus() != SamplingStatus.Submitted &&
                req.getStatus() != SamplingStatus.Approved) {
            throw new IllegalStateException("매칭 요청은 제출된 상태에서만 가능합니다.");
        }

        Long studentOrgId = req.getUser().getStudentOrg().getStudentOrgId();

        for (SamplingSelectedCompany sc : req.getSelectedCompanies()) {
            CollaborationMatchRequest match = CollaborationMatchRequest.builder()
                    .studentOrgId(studentOrgId)
                    .companyId(sc.getCompany().getCompanyId())
                    .eventTitle(req.getEventTitle())
                    .desiredDate(req.getEventStartDate())
                    .industry(req.getIndustry())
                    .collaborationType(CollaborationType.Sampling.name())
                    .status(MatchingStatus.Requested)
                    .requestedAt(LocalDateTime.now())
                    .build();

            collaborationMatchRequestRepository.save(match);
        }

        req.setStatus(SamplingStatus.MatchingRequested);
    }
}