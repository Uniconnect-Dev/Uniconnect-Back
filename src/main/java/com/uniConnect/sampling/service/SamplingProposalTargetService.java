package com.uniConnect.sampling.service;

import com.uniConnect.sampling.dto.SamplingProposalTargetRequestDto;
import com.uniConnect.sampling.entity.SamplingProposal;
import com.uniConnect.sampling.entity.SamplingTargetKeyword;
import com.uniConnect.sampling.entity.SamplingTargetSelection;
import com.uniConnect.sampling.enums.SamplingTargetCategory;
import com.uniConnect.sampling.repository.SamplingProposalRepository;
import com.uniConnect.sampling.repository.SamplingTargetKeywordRepository;
import com.uniConnect.sampling.repository.SamplingTargetSelectionRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SamplingProposalTargetService {

    private final SamplingProposalRepository samplingProposalRepository;
    private final SamplingTargetKeywordRepository keywordRepository;
    private final SamplingTargetSelectionRepository selectionRepository;

    private static final int MAX = 5;

    public void saveTargets(Long userId, SamplingProposalTargetRequestDto dto) {

        SamplingProposal proposal = samplingProposalRepository.findById(dto.getSamplingProposalId())
                .orElseThrow(() -> new IllegalArgumentException("샘플링 요청이 없습니다."));

        if (!proposal.getCreator().getUserId().equals(userId)) {
            throw new IllegalStateException("요청 권한이 없습니다.");
        }

        // 기존 선택 전부 삭제
        selectionRepository.deleteBySamplingProposal_ProposalId(
                proposal.getProposalId()
        );

        // 사전 정의 키워드
        saveByIds(proposal, dto.getBasicInfoKeywordIds());
        saveByIds(proposal, dto.getLifestyleKeywordIds());
        saveByIds(proposal, dto.getEventNatureKeywordIds());

        saveCustomKeywords(proposal, dto.getCustomKeywords());
    }

    private void saveByIds(SamplingProposal proposal, List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        if (ids.size() > MAX) {
            throw new IllegalArgumentException("카테고리별 최대 5개까지 선택 가능합니다.");
        }

        List<SamplingTargetKeyword> keywords = keywordRepository.findAllById(ids);

        List<SamplingTargetSelection> selections = keywords.stream()
                .map(k -> SamplingTargetSelection.builder()
                        .samplingProposal(proposal)
                        .targetKeyword(k)
                        .category(k.getCategory())
                        .selectedLabel(k.getLabel())
                        .build())
                .toList();

        selectionRepository.saveAll(selections);
    }

    private void saveCustomKeywords(SamplingProposal proposal, List<String> labels) {
        if (labels == null || labels.isEmpty()) return;
        if (labels.size() > MAX) {
            throw new IllegalArgumentException("기타 키워드는 최대 5개까지 가능합니다.");
        }

        for (String label : labels) {
            SamplingTargetKeyword keyword = SamplingTargetKeyword.builder()
                    .category(SamplingTargetCategory.Etc)
                    .label(label)
                    .isActive(false)
                    .build();

            keywordRepository.save(keyword);

            SamplingTargetSelection selection = SamplingTargetSelection.builder()
                    .samplingProposal(proposal)
                    .targetKeyword(keyword)
                    .category(SamplingTargetCategory.Etc)
                    .selectedLabel(label)
                    .build();

            selectionRepository.save(selection);
        }
    }

    private void validateSize(List<Long> list) {
        if (list != null && list.size() > MAX) {
            throw new IllegalArgumentException("카테고리별 최대 5개까지 선택 가능합니다.");
        }
    }

    private void addAll(List<Long> target, List<Long> source) {
        if (source != null) target.addAll(source);
    }
}
