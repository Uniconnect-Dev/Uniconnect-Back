package com.uniConnect.company.repository;

import com.uniConnect.studentOrg.entity.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface CompanyKeywordRepository extends JpaRepository<Hashtag, Long> {

    @Query(
            value = """
    select hashtag_id
    from company_keywords
    where company_id = :companyId
  """,
            nativeQuery = true
    )
    List<Long> findHashtagIdsByCompanyId(@Param("companyId") Long companyId);
}
