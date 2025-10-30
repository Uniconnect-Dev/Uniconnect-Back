package com.uniConnect.sampling.service;

import com.uniConnect.global.exception.CustomException;
import com.uniConnect.global.exception.ErrorCode;
import com.uniConnect.sampling.dto.SamplingTargetOptionDto;
import com.uniConnect.sampling.dto.SamplingTargetRequestDto;
import com.uniConnect.sampling.dto.SamplingTargetResponseDto;
import com.uniConnect.sampling.dto.response.SamplingTargetKeywordResponse;
import com.uniConnect.sampling.entity.SamplingRequest;
import com.uniConnect.sampling.entity.SamplingTargetKeyword;
import com.uniConnect.sampling.entity.SamplingTargetSelection;
import com.uniConnect.sampling.enums.SamplingTargetCategory;
import com.uniConnect.sampling.repository.SamplingRequestRepository;
import com.uniConnect.sampling.repository.SamplingTargetKeywordRepository;
import com.uniConnect.sampling.repository.SamplingTargetSelectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SamplingTargetService {

    private final SamplingRequestRepository requestRepository;
    private final SamplingTargetKeywordRepository keywordRepository;
    private final SamplingTargetSelectionRepository selectionRepository;

    private static final int MAX = 5;

    @Transactional
    public SamplingTargetResponseDto saveTargetSelection(SamplingTargetRequestDto dto) {

        SamplingRequest req = requestRepository.findById(dto.getRequestId())
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        if (dto.getCategory1KeywordIds() != null && dto.getCategory1KeywordIds().size() > MAX)
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        if (dto.getCategory2KeywordIds() != null && dto.getCategory2KeywordIds().size() > MAX)
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);

        List<SamplingTargetSelection> oldSelections =
                selectionRepository.findBySamplingRequest_SamplingRequestId(req.getSamplingRequestId());
        selectionRepository.deleteAll(oldSelections);

        List<Long> allIds = new ArrayList<>();
        if (dto.getCategory1KeywordIds() != null) allIds.addAll(dto.getCategory1KeywordIds());
        if (dto.getCategory2KeywordIds() != null) allIds.addAll(dto.getCategory2KeywordIds());
        if (dto.getCategory3KeywordIds() != null) allIds.addAll(dto.getCategory3KeywordIds());
        if (dto.getIndustryOfficialKeywordIds() != null) allIds.addAll(dto.getIndustryOfficialKeywordIds());

        List<SamplingTargetKeyword> keywords = allIds.isEmpty()
                ? List.of()
                : keywordRepository.findAllById(allIds);

        // 4) Selection 재구성하여 저장
        List<SamplingTargetSelection> toSave = keywords.stream()
                .map(k -> SamplingTargetSelection.builder()
                        .samplingRequest(req)
                        .targetKeyword(k)
                        .category(k.getCategory())
                        .selectedLabel(k.getLabel())
                        .build()
                )
                .collect(Collectors.toList());
        selectionRepository.saveAll(toSave);

        // 5) 저장 후 결과 조회/반환
        return getTargetSelection(dto.getRequestId());
    }

    /**
     * 현재 선택 + 옵션 조회
     */
    @Transactional(readOnly = true)
    public SamplingTargetResponseDto getTargetSelection(Long requestId) {
        SamplingRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        List<SamplingTargetSelection> selections =
                selectionRepository.findBySamplingRequest_SamplingRequestId(req.getSamplingRequestId());

        Map<SamplingTargetCategory, List<String>> selected = new EnumMap<>(SamplingTargetCategory.class);
        for (SamplingTargetCategory c : SamplingTargetCategory.values()) selected.put(c, new ArrayList<>());

        for (SamplingTargetSelection s : selections) {
            selected.get(s.getCategory()).add(s.getTargetKeyword().getLabel());
        }

        List<SamplingTargetOptionDto> c1Options = toOptionList(SamplingTargetCategory.BasicInfo);
        List<SamplingTargetOptionDto> c2Options = toOptionList(SamplingTargetCategory.Lifestyle);
        List<SamplingTargetOptionDto> c3Options = toOptionList(SamplingTargetCategory.EventNature);
        List<SamplingTargetOptionDto> indOptions = toOptionList(SamplingTargetCategory.IndustryOfficial);

        return SamplingTargetResponseDto.builder()
                .requestId(requestId)
                .category1Keywords(selected.get(SamplingTargetCategory.BasicInfo))
                .category2Keywords(selected.get(SamplingTargetCategory.Lifestyle))
                .category3Keywords(selected.get(SamplingTargetCategory.EventNature))
                .industryOfficialKeywords(selected.get(SamplingTargetCategory.IndustryOfficial))
                .category1Options(c1Options)
                .category2Options(c2Options)
                .category3Options(c3Options)
                .industryOfficialOptions(indOptions)
                .build();
    }

    /** 카테고리별 전체 옵션 목록 반환 */
    private List<SamplingTargetOptionDto> toOptionList(SamplingTargetCategory category) {
        return keywordRepository.findByCategoryAndIsActiveTrueOrderByLabelAsc(category)
                .stream()
                .map(k -> SamplingTargetOptionDto.builder()
                        .keywordId(k.getTargetKeywordId())
                        .label(k.getLabel())
                        .description(k.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    /** 단일 카테고리 키워드 리스트 조회 */
    @Transactional(readOnly = true)
    public List<SamplingTargetKeywordResponse> getKeywordsByCategory(SamplingTargetCategory category) {
        return keywordRepository.findByCategoryAndIsActiveTrueOrderByLabelAsc(category)
                .stream()
                .map(k -> new SamplingTargetKeywordResponse(k.getTargetKeywordId(), k.getLabel()))
                .collect(Collectors.toList());
    }
}
