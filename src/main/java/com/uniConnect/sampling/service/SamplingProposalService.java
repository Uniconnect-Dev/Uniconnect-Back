package com.uniConnect.sampling.service;

import com.uniConnect.member.entity.User;
import com.uniConnect.member.repository.UserRepository;
import com.uniConnect.sampling.dto.SamplingProposalCreateRequest;
import com.uniConnect.sampling.entity.SamplingProposal;
import com.uniConnect.sampling.enums.SamplingStatus;
import com.uniConnect.sampling.repository.SamplingProposalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SamplingProposalService {

    private final SamplingProposalRepository samplingProposalRepository;
    private final UserRepository userRepository;

    /**
     * 1페이지: 기업 샘플링 요청 생성
     */
    public Long createProposal(Long userId, SamplingProposalCreateRequest request) {

        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("기업 계정을 찾을 수 없습니다."));

        SamplingProposal proposal = SamplingProposal.builder()
                .creator(creator)
                .productName(request.getProductName())
                .industry(request.getIndustry())
                .samplingPurpose(request.getSamplingPurpose())
                .samplingStartDate(request.getSamplingStartDate())
                .samplingEndDate(request.getSamplingEndDate())
                .productCount(request.getProductCount())
                .detailRequest(request.getDetailRequest())
                .status(SamplingStatus.Draft)
                .build();

        samplingProposalRepository.save(proposal);
        return proposal.getProposalId();
    }
}
