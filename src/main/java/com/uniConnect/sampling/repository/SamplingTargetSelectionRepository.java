package com.uniConnect.sampling.repository;

import com.uniConnect.member.entity.User;
import com.uniConnect.sampling.entity.SamplingTargetSelection;
import com.uniConnect.sampling.enums.SamplingTargetCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SamplingTargetSelectionRepository extends JpaRepository<SamplingTargetSelection, Long> {

    List<SamplingTargetSelection> findByUser_UserId(Long userId);

    Optional<SamplingTargetSelection> findByUser_UserIdAndCategory(Long userId, SamplingTargetCategory category);
}
