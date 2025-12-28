package com.uniConnect.sampling.repository;

import com.uniConnect.sampling.entity.SamplingTargetKeyword;
import com.uniConnect.sampling.enums.SamplingTargetCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SamplingTargetKeywordRepository extends JpaRepository<SamplingTargetKeyword, Long> {
    List<SamplingTargetKeyword> findByCategory(SamplingTargetCategory category);
    List<SamplingTargetKeyword> findByCategoryAndIsActiveTrueOrderByLabelAsc(SamplingTargetCategory category);
}