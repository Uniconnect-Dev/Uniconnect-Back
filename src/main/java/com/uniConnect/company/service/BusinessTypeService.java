package com.uniConnect.company.service;

import com.uniConnect.company.dto.BusinessTypeDto;
import com.uniConnect.company.entity.BusinessType;
import com.uniConnect.company.repository.BusinessTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusinessTypeService {

    private final BusinessTypeRepository businessTypeRepository;

    /**
     * 모든 업태 조회
     */
    public List<BusinessTypeDto> getAllBusinessTypes() {
        return businessTypeRepository.findAll()
                .stream()
                .map(BusinessTypeDto::of)
                .toList();
    }

    /**
     * 업태 ID로 조회
     */
    public BusinessTypeDto getBusinessType(Long businessTypeId) {
        return businessTypeRepository.findById(businessTypeId)
                .map(BusinessTypeDto::of)
                .orElseThrow(() -> new IllegalArgumentException("업태를 찾을 수 없습니다."));
    }

    /**
     * 업태명으로 조회
     */
    public BusinessTypeDto getBusinessTypeByName(String name) {
        return businessTypeRepository.findByName(name)
                .map(BusinessTypeDto::of)
                .orElseThrow(() -> new IllegalArgumentException("업태를 찾을 수 없습니다."));
    }
}