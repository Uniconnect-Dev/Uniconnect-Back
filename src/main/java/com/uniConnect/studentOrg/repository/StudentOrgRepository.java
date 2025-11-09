package com.uniConnect.studentOrg.repository;

import com.uniConnect.studentOrg.entity.StudentOrg;
import com.uniConnect.studentOrg.enums.CollaborationType;
import com.uniConnect.studentOrg.enums.OrganizationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentOrgRepository extends JpaRepository<StudentOrg, Long> {

    Optional<StudentOrg> findByUser_UserId(Long userId);

    /**
     * 협업 이력이 있는 단체만 불러오고,
     * 검색어 / 협업유형 / 단체유형 필터를 적용하는 커스텀 조회
     */
    @Query("""
    SELECT DISTINCT o
    FROM StudentOrg o
    LEFT JOIN FETCH o.histories h
    WHERE (:keyword IS NULL OR :keyword = '' 
           OR o.organizationName LIKE %:keyword%
           OR o.schoolName LIKE %:keyword%)
      AND (:collabType IS NULL OR o.collaborationType = :collabType)
      AND (:orgType IS NULL OR o.organizationType = :orgType)
    """)
    List<StudentOrg> searchProfiles(
            @Param("keyword") String keyword,
            @Param("collabType") CollaborationType collabType,
            @Param("orgType") OrganizationType orgType
    );
}
