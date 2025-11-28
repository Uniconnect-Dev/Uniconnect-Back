package com.uniConnect.sampling.service;

import com.uniConnect.sampling.dto.*;
import com.uniConnect.sampling.repository.*;
import com.uniConnect.sampling.entity.*;
import com.uniConnect.studentOrg.entity.StudentOrg;
import com.uniConnect.sampling.enums.SamplingStatus;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SamplingMatchService {

    private final StudentOrgQueryRepository studentOrgQueryRepository;
    private final SamplingRequestRepository samplingRequestRepository;
    private final SamplingTargetKeywordRepository targetKeywordRepository;
    private final SamplingTargetSelectionRepository targetSelectionRepository;
    private final SamplingMatchedOrgRepository matchedOrgRepository;

    /**
     * 매칭된 학생단체 리스트 조회
     */
    public List<StudentOrgSummaryResponse> getMatchedStudentOrgs(
            Long samplingRequestId,
            String schoolName,
            Integer verificationLevel,
            int baseUnitCost,
            int reportOptionFee,
            int operationFee
    ) {
        SamplingRequest request = samplingRequestRepository.findById(samplingRequestId)
                .orElseThrow(() -> new RuntimeException("샘플링 요청을 찾을 수 없습니다."));

        int participants = request.getRequestedQuantity();

        List<StudentOrg> orgs =
                studentOrgQueryRepository.findMatchingStudentOrgs(
                        schoolName,
                        verificationLevel
                );

        return orgs.stream()
                .map(org -> {
                    int cost = (participants * baseUnitCost)
                            + reportOptionFee
                            + operationFee;

                    String range = String.format("%,d원 ~ %,d원", cost - 100_000, cost + 100_000);

                    return StudentOrgSummaryResponse.builder()
                            .studentOrgId(org.getStudentOrgId())
                            .organizationName(org.getOrganizationName())
                            .schoolName(org.getSchoolName())
                            .expectedParticipants(participants)
                            .estimatedCostRange(range)
                            .logoUrl(org.getLogoUrl())
                            .build();
                })
                .toList();
    }


    /**
     * 단체 상세정보 조회
     */
    @Transactional(readOnly = true)
    public StudentOrgDetailResponse getStudentOrgDetail(
            Long samplingRequestId,
            Long orgId,
            int baseUnitCost,
            int reportOptionFee,
            int operationFee
    ) {
        SamplingRequest request = samplingRequestRepository.findById(samplingRequestId)
                .orElseThrow(() -> new RuntimeException("샘플링 요청을 찾을 수 없습니다."));

        StudentOrg org = studentOrgQueryRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("학생단체를 찾을 수 없습니다."));

        int participants = request.getRequestedQuantity();

        int cost = (participants * baseUnitCost)
                + reportOptionFee
                + operationFee;

        String range = String.format("%,d원 ~ %,d원", cost - 100_000, cost + 100_000);

        // availability 매핑
        List<StudentOrgDetailResponse.AvailabilityInfo> availabilities =
                org.getAvailabilities().stream()
                        .map(a -> StudentOrgDetailResponse.AvailabilityInfo.builder()
                                .eventName(a.getEventName())
                                .startDate(a.getStartDate())
                                .endDate(a.getEndDate())
                                .description(a.getDescription())
                                .build()
                        )
                        .toList();

        return StudentOrgDetailResponse.builder()
                .studentOrgId(org.getStudentOrgId())
                .organizationName(org.getOrganizationName())
                .schoolName(org.getSchoolName())
                .managerName(org.getManagerName())
                .phone(org.getPhone())
                .email(org.getEmail())
                .availabilities(availabilities)
                .estimatedCostRange(range)
                .build();
    }


    /**
     * 선택한 학생단체 총 예상비용 계산
     */
    public EstimatedTotalCostResponse calculateTotalEstimatedCost(
            Long samplingRequestId,
            List<Long> selectedOrgIds,
            int baseUnitCost,
            int reportOptionFee,
            int operationFee
    ) {
        SamplingRequest request = samplingRequestRepository.findById(samplingRequestId)
                .orElseThrow(() -> new RuntimeException("샘플링 요청을 찾을 수 없습니다."));

        int participants = request.getRequestedQuantity();

        int unitCost = (participants * baseUnitCost)
                + reportOptionFee
                + operationFee;

        int total = unitCost * selectedOrgIds.size();

        return EstimatedTotalCostResponse.of(total);
    }


    /**
     * 매칭 요청 제출
     */
    @Transactional
    public SamplingMatchSubmitResponse submitSamplingMatch(SamplingMatchRequestDto dto) {

        SamplingRequest request = samplingRequestRepository.findById(dto.getSamplingRequestId())
                .orElseThrow(() ->
                        new IllegalArgumentException("해당 샘플링 요청을 찾을 수 없습니다.")
                );

        // 1) 타깃 키워드 저장
        targetSelectionRepository.deleteAll(request.getSelections());
        request.getSelections().clear();

        List<SamplingTargetKeyword> keywords =
                targetKeywordRepository.findAllById(dto.getSelectedKeywordIds());

        for (SamplingTargetKeyword keyword : keywords) {
            request.addSelection(SamplingTargetSelection.of(request, keyword));
        }

        // 2) 학생단체 선택 저장
        matchedOrgRepository.deleteAll(request.getMatchedOrgs());
        request.getMatchedOrgs().clear();

        List<StudentOrg> selectedOrgs =
                studentOrgQueryRepository.findAllById(dto.getSelectedOrgIds());

        for (StudentOrg org : selectedOrgs) {
            request.addMatchedOrg(SamplingMatchedOrg.of(request, org));
        }

        // 3) 상태 변경
        request.setStatus(SamplingStatus.Submitted);
        samplingRequestRepository.save(request);

        return SamplingMatchSubmitResponse.from(request);
    }
}