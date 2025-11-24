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
    Optional<StudentOrg> findByUsers_UserId(Long userId);
    boolean existsByUsers_UserId(Long userId);

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

    @Query("""
    SELECT DISTINCT o
    FROM StudentOrg o
    LEFT JOIN FETCH o.keywords k
    LEFT JOIN FETCH k.hashtag h
    LEFT JOIN FETCH o.availabilities av
    """)
    List<StudentOrg> findAllWithKeywordsAndAvailabilities();
}
