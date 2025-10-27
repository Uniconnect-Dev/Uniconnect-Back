package com.uniConnect.sampling.service;

import com.uniConnect.global.exception.CustomException;
import com.uniConnect.global.exception.ErrorCode;
import com.uniConnect.member.entity.LocalCredential;
import com.uniConnect.member.entity.User;
import com.uniConnect.member.repository.LocalCredentialRepository;
import com.uniConnect.sampling.dto.*;
import com.uniConnect.sampling.entity.SamplingTargetKeyword;
import com.uniConnect.sampling.entity.SamplingTargetSelection;
import com.uniConnect.sampling.enums.SamplingTargetCategory;
import com.uniConnect.sampling.repository.SamplingTargetKeywordRepository;
import com.uniConnect.sampling.repository.SamplingTargetSelectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SamplingTargetService {

    private final LocalCredentialRepository localCredentialRepository;
    private final SamplingTargetKeywordRepository keywordRepository;
    private final SamplingTargetSelectionRepository selectionRepository;

    private static final int MAX = 5;

    /**
     * 타깃 키워드 저장 + 저장 후 결과 반환
     */
    @Transactional
    public SamplingTargetResponseDto saveTargetSelection(SamplingTargetRequestDto requestDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loginId = (String) authentication.getPrincipal();

        User user = localCredentialRepository.findByLoginId(loginId)
                .map(LocalCredential::getUser)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        upsert(user, SamplingTargetCategory.Category1, requestDto.getCategory1KeywordIds());
        upsert(user, SamplingTargetCategory.Category2, requestDto.getCategory2KeywordIds());
        upsert(user, SamplingTargetCategory.Category3, requestDto.getCategory3KeywordIds());

        return getTargetSelection(user);
    }

    /**
     * 로그인 사용자 기준 조회 (컨트롤러에서 사용)
     */
    @Transactional(readOnly = true)
    public SamplingTargetResponseDto getTargetSelection() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loginId = (String) authentication.getPrincipal();

        User user = localCredentialRepository.findByLoginId(loginId)
                .map(LocalCredential::getUser)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return getTargetSelection(user);
    }

    /**
     * 내부 공용 함수: 특정 사용자 기준 조회
     */
    @Transactional(readOnly = true)
    private SamplingTargetResponseDto getTargetSelection(User user) {
        Map<SamplingTargetCategory, List<String>> selected = new EnumMap<>(SamplingTargetCategory.class);
        for (SamplingTargetCategory c : SamplingTargetCategory.values()) selected.put(c, new ArrayList<>());

        List<SamplingTargetSelection> selections = selectionRepository.findByUser_UserId(user.getUserId());
        for (SamplingTargetSelection s : selections) {
            List<String> list = Arrays.stream(s.getSelectedKeywordsCsv().split(","))
                    .map(String::trim)
                    .filter(str -> !str.isEmpty())
                    .toList();
            selected.put(s.getCategory(), list);
        }

        return SamplingTargetResponseDto.builder()
                .category1Keywords(selected.get(SamplingTargetCategory.Category1))
                .category2Keywords(selected.get(SamplingTargetCategory.Category2))
                .category3Keywords(selected.get(SamplingTargetCategory.Category3))
                .category1Options(toOptionList(SamplingTargetCategory.Category1))
                .category2Options(toOptionList(SamplingTargetCategory.Category2))
                .category3Options(toOptionList(SamplingTargetCategory.Category3))
                .build();
    }

    /**
     * DB 갱신 or 신규 저장
     */
    private void upsert(User user, SamplingTargetCategory category, List<Long> ids) {
        if (ids == null || ids.isEmpty() || ids.size() > MAX)
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);

        List<SamplingTargetKeyword> keywords = keywordRepository.findByTargetKeywordIdIn(ids);

        String csv = keywords.stream()
                .map(SamplingTargetKeyword::getLabel)
                .collect(Collectors.joining(","));

        SamplingTargetSelection selection = selectionRepository
                .findByUser_UserIdAndCategory(user.getUserId(), category)
                .orElseGet(() -> SamplingTargetSelection.builder()
                        .user(user)
                        .category(category)
                        .build());

        selection.setSelectedKeywordsCsv(csv);
        selectionRepository.save(selection);
    }

    /**
     * 카테고리별 전체 옵션 목록 반환
     */
    private List<SamplingTargetOptionDto> toOptionList(SamplingTargetCategory category) {
        return keywordRepository.findByCategoryAndIsActiveTrueOrderByLabelAsc(category)
                .stream()
                .map(k -> SamplingTargetOptionDto.builder()
                        .keywordId(k.getTargetKeywordId())
                        .label(k.getLabel())
                        .description(k.getDescription())
                        .build())
                .toList();
    }
}
