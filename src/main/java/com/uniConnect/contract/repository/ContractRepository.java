package com.uniConnect.contract.repository;

import com.uniConnect.contract.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    List<Contract> findByCollaboration_MatchRequest_Company_CompanyId(Long companyId);
    List<Contract> findByCollaboration_MatchRequest_StudentOrg_StudentOrgId(
            Long studentOrgId
    );
}
