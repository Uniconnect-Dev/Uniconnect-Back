package com.uniConnect.studentOrg.repository;

import com.uniConnect.studentOrg.entity.StudentOrgAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface StudentOrgAvailabilityRepository extends JpaRepository<StudentOrgAvailability, Long> {

    @Query("""
        SELECT a
        FROM StudentOrgAvailability a
        WHERE a.studentOrg.studentOrgId = :orgId
          AND a.startDate <= :endDate
          AND a.endDate >= :startDate
        ORDER BY a.startDate ASC
        """)
    List<StudentOrgAvailability> findCalendar(
            @Param("orgId") Long orgId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}