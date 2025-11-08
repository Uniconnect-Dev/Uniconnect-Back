package com.uniConnect.sampling.service;

import com.uniConnect.sampling.dto.*;
import com.uniConnect.sampling.repository.StudentOrgQueryRepository;
import com.uniConnect.studentOrg.entity.StudentOrg;
import com.uniConnect.studentOrg.entity.StudentOrgAvailability;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SamplingMatchService {

    private final StudentOrgQueryRepository StudentOrgQueryRepository;

    /**
     * 기업 조건 기반 매칭된 학생단체 리스트 조회
     */
    public List<StudentOrgSummaryResponse> getMatchedStudentOrgs(String schoolName, Integer verificationLevel,
                                                                 int baseUnitCost, int reportOptionFee, int operationFee) {

        List<StudentOrg> orgs = StudentOrgQueryRepository.findMatchingStudentOrgs(schoolName, verificationLevel);

        return orgs.stream().map(org -> {
            int participants = (int) (Math.random() * 100) + 20; // 예상 참여인원(예시)
            int cost = (participants * baseUnitCost) + reportOptionFee + operationFee;
            String range = String.format("%,d원 ~ %,d원", cost - 100_000, cost + 100_000);

            return StudentOrgSummaryResponse.builder()
                    .studentOrgId(org.getStudentOrgId())
                    .organizationName(org.getOrganizationName())
                    .schoolName(org.getSchoolName())
                    .expectedParticipants(participants)
                    .estimatedCostRange(range)
                    .logoUrl(org.getLogoUrl())
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * 단체 상세정보 조회
     */
    @Transactional(readOnly = true)
    public StudentOrgDetailResponse getStudentOrgDetail(Long orgId, int baseUnitCost, int reportOptionFee, int operationFee) {
        StudentOrg org = StudentOrgQueryRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("해당 학생단체를 찾을 수 없습니다."));

        int participants = (int) (Math.random() * 100) + 20;
        int cost = (participants * baseUnitCost) + reportOptionFee + operationFee;
        String range = String.format("%,d원 ~ %,d원", cost - 100_000, cost + 100_000);

        List<StudentOrgDetailResponse.AvailabilityInfo> availabilities = org.getAvailabilities().stream()
                .map(a -> StudentOrgDetailResponse.AvailabilityInfo.builder()
                        .eventName(a.getEventName())
                        .startDate(a.getStartDate())
                        .endDate(a.getEndDate())
                        .description(a.getDescription())
                        .build())
                .collect(Collectors.toList());

        return StudentOrgDetailResponse.builder()
                .studentOrgId(org.getStudentOrgId())
                .organizationName(org.getOrganizationName())
                .schoolName(org.getSchoolName())
                .managerName(org.getManagerName())
                .phone(org.getPhone())
                .email(org.getEmail())
                .description("해당 단체의 소개 및 활동 내역입니다.")
                .availabilities(availabilities)
                .estimatedCostRange(range)
                .build();
    }

    /**
     * 선택한 학생단체들 총 예상비용 계산
     */
    public EstimatedTotalCostResponse calculateTotalEstimatedCost(List<Long> selectedOrgIds,
                                                                  int baseUnitCost, int reportOptionFee, int operationFee) {
        List<StudentOrg> orgs = StudentOrgQueryRepository.findAllById(selectedOrgIds);

        int total = 0;
        for (StudentOrg org : orgs) {
            int participants = (int) (Math.random() * 100) + 20;
            int cost = (participants * baseUnitCost) + reportOptionFee + operationFee;
            total += cost;
        }

        int min = total - 100_000;
        int max = total + 100_000;

        String message = String.format("선택하신 단체들의 예상 이용료는 약 %,d원 ~ %,d원입니다. (확정 시 정확 금액 안내)", min, max);

        return EstimatedTotalCostResponse.builder()
                .minEstimatedTotal(min)
                .maxEstimatedTotal(max)
                .message(message)
                .build();
    }
}