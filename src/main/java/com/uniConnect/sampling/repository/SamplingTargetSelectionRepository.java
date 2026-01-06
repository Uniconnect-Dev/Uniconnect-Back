package com.uniConnect.sampling.repository;

import com.uniConnect.sampling.entity.SamplingTargetSelection;
import com.uniConnect.sampling.enums.SamplingTargetCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SamplingTargetSelectionRepository extends JpaRepository<SamplingTargetSelection, Long> {
    List<SamplingTargetSelection> findBySamplingRequest_User_UserId(Long userId);
    List<SamplingTargetSelection> findBySamplingRequest_User_UserIdAndCategory(Long userId, SamplingTargetCategory category);
    List<SamplingTargetSelection> findBySamplingRequest_SamplingRequestId(Long samplingRequestId);
    void deleteBySamplingProposal_ProposalId(Long proposalId);
    @Query("""
    select sts.selectedLabel
    from SamplingTargetSelection sts
    where sts.samplingProposal.proposalId = :proposalId
""")
    List<String> findSelectedLabelsBySamplingProposalId(Long proposalId);

    List<SamplingTargetSelection>
    findBySamplingProposal_ProposalId(Long proposalId);

}
