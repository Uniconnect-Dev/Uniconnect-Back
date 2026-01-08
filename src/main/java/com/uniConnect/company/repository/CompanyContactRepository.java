package com.uniConnect.company.repository;

import com.uniConnect.company.entity.CompanyContact;
import com.uniConnect.company.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyContactRepository extends JpaRepository<CompanyContact, Long> {

    /**
     * 특정 Company의 mainContactId를 통해 CompanyContact 조회
     * Company.mainContactId = CompanyContact.pk
     */
    Optional<CompanyContact> findByCompanyMainContactIdAndCompany(Long companyContactId, Company company);

    /**
     * JPQL을 사용한 방식
     * 특정 Company의 mainContact 조회
     */
    @Query("""
        SELECT cc
        FROM CompanyContact cc
        WHERE cc.company = :company
        AND cc.contactId = :mainContactId
        """)
    Optional<CompanyContact> findMainContactByCompany(
            @Param("company") Company company,
            @Param("mainContactId") Long mainContactId);

}