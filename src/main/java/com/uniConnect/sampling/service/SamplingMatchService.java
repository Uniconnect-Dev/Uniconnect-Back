package com.uniConnect.sampling.service;

import com.uniConnect.campaign.entity.Campaign;
import com.uniConnect.campaign.enums.MatchingStatus;
import com.uniConnect.campaign.repository.CampaignRepository;

import com.uniConnect.company.entity.Company;
import com.uniConnect.company.repository.CompanyRepository;
import com.uniConnect.campaign.enums.CampaignStatus;
import com.uniConnect.matching.entity.CollaborationMatchRequest;
import com.uniConnect.matching.enums.MatchSender;
import com.uniConnect.matching.repository.CollaborationMatchRequestRepository;

import com.uniConnect.sampling.dto.*;
import com.uniConnect.sampling.entity.SamplingProposal;
import com.uniConnect.sampling.entity.SamplingTargetSelection;
import com.uniConnect.sampling.enums.SamplingStatus;
import com.uniConnect.sampling.repository.SamplingProposalRepository;

import com.uniConnect.studentOrg.entity.Hashtag;
import com.uniConnect.studentOrg.enums.CollaborationType;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SamplingMatchService {

    private final CampaignRepository campaignRepository;
    private final SamplingProposalRepository samplingProposalRepository;
    private final CompanyRepository companyRepository;
    private final CollaborationMatchRequestRepository matchRequestRepository;

    /* =====================================================
      3페이지: 매칭 가능한 학생단체 목록 조회
    ===================================================== */
    @Transactional(readOnly = true)
    public List<StudentOrgSummaryResponse> getMatchedStudentOrgs(
            Long samplingProposalId
    ) {
        SamplingProposal proposal = samplingProposalRepository.findById(samplingProposalId)
                .orElseThrow(() -> new IllegalArgumentException("샘플링 요청이 없습니다."));

        List<String> proposalLabels = proposal.getSelections().stream()
                .map(SamplingTargetSelection::getSelectedLabel)
                .map(String::toLowerCase)
                .toList();

        if (proposalLabels.isEmpty()) {
            return List.of();
        }

        List<Campaign> candidates =
                campaignRepository.findSamplingCampaignCandidates(
                        proposal.getSamplingStartDate(),
                        proposal.getSamplingEndDate(),
                        CollaborationType.Sampling,
                        CampaignStatus.Submitted
                );

        return candidates.stream()
                .filter(campaign -> {
                    List<String> campaignLabels =
                            campaign.getCampaignTargets().stream()
                                    .map(ct -> ct.getHashtag().getName().toLowerCase())
                                    .toList();

                    return campaignLabels.stream()
                            .anyMatch(proposalLabels::contains);
                })
                .map(campaign -> {
                    int expectedParticipants = campaign.getExpectedParticipants();
                    int expectedExposures = campaign.getExpectedExposures();
                    int boothFee = campaign.getBoothFee();

                    double scaleFactor = calculateScaleFactor(expectedParticipants);

                    int estimatedCost = (int) (
                            (100 * expectedParticipants + 10 + expectedExposures)
                                    * scaleFactor
                                    + boothFee
                    );

                    return StudentOrgSummaryResponse.builder()
                            .studentOrgId(campaign.getStudentOrg().getStudentOrgId())
                            .organizationName(campaign.getStudentOrg().getOrganizationName())
                            .schoolName(campaign.getStudentOrg().getSchoolName())
                            .campaignName(campaign.getName())
                            .matchedTags(
                                    campaign.getCampaignTargets().stream()
                                            .map(ct -> ct.getHashtag().getName())
                                            .toList()
                            )
                            .expectedParticipants(expectedParticipants)
                            .estimatedCostRange(
                                    String.format("%,d원 ~ %,d원",
                                            estimatedCost - 100_000,
                                            estimatedCost + 100_000)
                            )
                            .build();
                })
                .toList();
    }

    /* =====================================================
      학생단체 상세 조회
      → Campaign 기준
    ===================================================== */
    @Transactional(readOnly = true)
    public StudentOrgDetailResponse getStudentOrgDetail(
            Long samplingProposalId,
            Long campaignId,
            int baseUnitCost,
            int reportOptionFee,
            int operationFee
    ) {
        samplingProposalRepository.findById(samplingProposalId)
                .orElseThrow(() -> new IllegalArgumentException("샘플링 요청이 없습니다."));

        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("캠페인을 찾을 수 없습니다."));

        int participants = campaign.getExpectedParticipants();
        int cost = (participants * baseUnitCost) + reportOptionFee + operationFee;

        return StudentOrgDetailResponse.builder()
                .studentOrgId(campaign.getStudentOrg().getStudentOrgId())
                .organizationName(campaign.getStudentOrg().getOrganizationName())
                .schoolName(campaign.getStudentOrg().getSchoolName())
                .managerName(campaign.getStudentOrg().getManagerName())
                .phone(campaign.getStudentOrg().getPhone())
                .email(campaign.getStudentOrg().getEmail())
                .estimatedCostRange(
                        String.format("%,d원 ~ %,d원",
                                cost - 100_000,
                                cost + 100_000)
                )
                .build();
    }

    /* =====================================================
      5페이지: 매칭 요청 제출 (기업 → 학생단체)
      ※ proposal 단위 요청
    ===================================================== */
    public SamplingMatchSubmitResponse submitSamplingMatch(
            Long userId,
            SamplingMatchRequestDto dto
    ) {
        SamplingProposal proposal = samplingProposalRepository.findById(dto.samplingProposalId())
                .orElseThrow(() -> new IllegalArgumentException("샘플링 요청이 없습니다."));

        if (!proposal.getCreator().getUserId().equals(userId)) {
            throw new IllegalStateException("요청 권한이 없습니다.");
        }

        if (dto.campaignIds() == null || dto.campaignIds().isEmpty()) {
            throw new IllegalArgumentException("선택된 캠페인이 없습니다.");
        }

        matchRequestRepository
                .deleteBySamplingProposal_ProposalId(proposal.getProposalId());

        for (Long campaignId : dto.campaignIds()) {
            Campaign campaign = campaignRepository.findById(campaignId)
                    .orElseThrow(() -> new IllegalArgumentException("캠페인 없음"));

            CollaborationMatchRequest match = CollaborationMatchRequest.builder()
                    .sender(MatchSender.COMPANY)
                    .collaborationType(CollaborationType.Sampling)
                    .samplingProposal(proposal)
                    .campaign(campaign)
                    .status(MatchingStatus.Requested)
                    .requestedAt(LocalDateTime.now())
                    .build();

            matchRequestRepository.save(match);
        }

        proposal.setStatus(SamplingStatus.Submitted);

        return SamplingMatchSubmitResponse.of(dto.campaignIds().size());
    }

    /* =========================
       총 예상 금액 계산
    ========================= */
    @Transactional(readOnly = true)
    public EstimatedTotalCostResponse calculateTotalEstimatedCost(
            Long samplingProposalId,
            List<Long> studentOrgIds,
            int baseUnitCost,
            int reportOptionFee,
            int operationFee
    ) {
        samplingProposalRepository.findById(samplingProposalId)
                .orElseThrow(() -> new IllegalArgumentException("샘플링 요청이 없습니다."));

        if (studentOrgIds == null || studentOrgIds.isEmpty()) {
            throw new IllegalArgumentException("학생단체가 선택되지 않았습니다.");
        }

        int totalBaseCost = 0;

        for (Long studentOrgId : studentOrgIds) {
            Campaign campaign = campaignRepository
                    .findTopByStudentOrg_StudentOrgIdAndCollaborationTypeOrderByCreatedAtDesc(
                            studentOrgId,
                            CollaborationType.Sampling
                    )
                    .orElseThrow(() -> new IllegalStateException("해당 학생단체의 캠페인이 없습니다."));

            int participants = campaign.getExpectedParticipants();

            totalBaseCost += (participants * baseUnitCost)
                    + reportOptionFee
                    + operationFee;
        }

        return EstimatedTotalCostResponse.of(totalBaseCost);
    }

    /* =========================
       단체별 예상 금액 계산
    ========================= */
    @Transactional(readOnly = true)
    public OrgEstimatedCostResponse calculateSingleOrgEstimatedCost(
            Long samplingProposalId,
            Long studentOrgId,
            int baseUnitCost,
            int reportOptionFee,
            int operationFee
    ) {
        samplingProposalRepository.findById(samplingProposalId)
                .orElseThrow(() -> new IllegalArgumentException("샘플링 요청이 없습니다."));

        Campaign campaign = campaignRepository
                .findTopByStudentOrg_StudentOrgIdAndCollaborationTypeOrderByCreatedAtDesc(
                        studentOrgId,
                        CollaborationType.Sampling
                )
                .orElseThrow(() -> new IllegalStateException("해당 학생단체의 캠페인이 없습니다."));

        int participants = campaign.getExpectedParticipants();

        int baseCost = (participants * baseUnitCost)
                + reportOptionFee
                + operationFee;

        return OrgEstimatedCostResponse.of(
                campaign.getStudentOrg().getStudentOrgId(),
                campaign.getStudentOrg().getOrganizationName(),
                participants,
                baseUnitCost,
                baseCost
        );
    }

    private double calculateScaleFactor(int expectedParticipants) {
        if (expectedParticipants < 500) {
            return 4.0;
        } else if (expectedParticipants < 1000) {
            return 3.0;
        } else if (expectedParticipants < 2000) {
            return 2.0;
        } else if (expectedParticipants < 3000) {
            return 1.7;
        } else {
            return 1.5;
        }
    }
}