package com.uniConnect.sampling.service;

import com.uniConnect.s3.S3FileService;
import com.uniConnect.sampling.dto.request.*;
import com.uniConnect.sampling.dto.response.SamplingRequestSummaryResponse;
import com.uniConnect.sampling.entity.*;
import com.uniConnect.sampling.enums.*;
import com.uniConnect.sampling.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SamplingRequestService {

    private final SamplingRequestRepository requestRepository;
    private final SamplingTargetKeywordRepository keywordRepository;
    private final SamplingTargetSelectionRepository selectionRepository;
    private final S3FileService s3FileService;

    @Value("${app.s3.bucket}")
    private String bucketName;

    /** Step 0: 샘플링 요청 초안 생성 */
    @Transactional
    public Long createDraft() {
        SamplingRequest request = SamplingRequest.builder()
                .status(SamplingRequestStatus.Draft)
                .build();
        return requestRepository.save(request).getSamplingRequestId();
    }

    /** Step 1: 단체 기본 정보 입력 */
    @Transactional
    public void updateStep1(Long id, OrgInfoRequest dto) {
        SamplingRequest req = find(id);
        req.setSchoolName(dto.schoolName());
        req.setOrgName(dto.orgName());
        req.setContactName(dto.contactName());
        req.setPhone(dto.phone());
        req.setEmail(dto.email());
    }

    /** Step 2: 단체 해시태그 선택 (BasicInfo, Lifestyle) */
    @Transactional
    public void updateStep2(Long id, TargetTagRequest dto) {
        SamplingRequest req = find(id);
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
    @Transactional
    public void updateStep3(Long id, EventInfoRequest dto) {
        SamplingRequest req = find(id);
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
    @Transactional
    public void updateStep4(Long id, IndustryRequest dto) {
        SamplingRequest req = find(id);
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
    @Transactional
    public String uploadProposal(Long id, MultipartFile file) throws Exception {
        SamplingRequest req = find(id);

        String key = "proposals/" + id + "_" + file.getOriginalFilename();

        s3FileService.upload(bucketName, key, file);

        String url = "https://" + bucketName + ".s3.amazonaws.com/" + key;
        req.setProposalFileUrl(url);
        return url;
    }

    /** Step 6: 최종 제출 */
    @Transactional
    public void submit(Long id) {
        SamplingRequest req = find(id);
        req.setStatus(SamplingRequestStatus.Submitted);
    }

    /** 요약 조회 */
    public SamplingRequestSummaryResponse getSummary(Long id) {
        SamplingRequest req = find(id);

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

    private SamplingRequest find(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sampling request not found"));
    }
}
