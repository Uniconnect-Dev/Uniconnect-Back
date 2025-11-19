package com.uniConnect.sampling.service;


import com.uniConnect.member.entity.User;
import com.uniConnect.member.repository.UserRepository;
import com.uniConnect.s3.S3FileService;
import com.uniConnect.sampling.dto.request.*;
import com.uniConnect.sampling.dto.response.*;
import com.uniConnect.sampling.dto.FeeResponse;
import com.uniConnect.sampling.entity.*;
import com.uniConnect.sampling.enums.*;
import com.uniConnect.sampling.enums.SamplingStatus;
import com.uniConnect.sampling.repository.*;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

    private static final int PER_SAMPLING_FEE = 50_000;   // 건당 수수료
    private static final int DEPOSIT = 50_000;            // 보증금

    @Value("${app.s3.bucket}")
    private String bucketName;

    /** Step 0: 샘플링 요청 초안 생성 */
    public SamplingRequest createDraft(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        SamplingRequest draft = SamplingRequest.builder()
                .user(user)
                .status(SamplingStatus.Draft)
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
        selectionRepository.deleteAll(req.getSelections());

        var allIds = dto.basicInfoTagIds();
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
        req.setEventTitle(dto.eventTitle());
        req.setEventDescription(dto.eventDescription());
        req.setRequestedQuantity(dto.requestedQuantity());
        req.setEventStartDate(dto.eventStartDate());
        req.setEventEndDate(dto.eventEndDate());

        var tags = keywordRepository.findAllById(dto.eventTagIds());
        tags.forEach(tag -> {
            if (tag.getCategory() == SamplingTargetCategory.EventNature) {
                req.addSelection(SamplingTargetSelection.builder()
                        .samplingRequest(req)
                        .targetKeyword(tag)
                        .category(tag.getCategory())
                        .selectedLabel(tag.getLabel())
                        .build());
            }
        });
    }

    /** Step 4: 산업군 및 공식 해시태그 선택 */
    public void updateStep4(Long id, IndustryRequest dto) {
        SamplingRequest req = findById(id);
        req.setIndustry(dto.industry());
        var tags = keywordRepository.findAllById(dto.industryTagIds());
        tags.forEach(tag -> {
            if (tag.getCategory() == SamplingTargetCategory.IndustryOfficial) {
                req.addSelection(SamplingTargetSelection.builder()
                        .samplingRequest(req)
                        .targetKeyword(tag)
                        .category(tag.getCategory())
                        .selectedLabel(tag.getLabel())
                        .build());
            }
        });
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
    public void signContract(Long id) {
        SamplingRequest req = findById(id);
        req.setStatus(SamplingStatus.ContractApprovalPending);
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

        req.setStatus(SamplingStatus.MatchingRequested);
    }
}