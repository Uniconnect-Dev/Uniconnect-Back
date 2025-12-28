package com.uniConnect.sampling.repository;

import com.uniConnect.studentOrg.entity.StudentOrg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDate;

@Repository
public interface StudentOrgQueryRepository extends JpaRepository<StudentOrg, Long> {


    @Query("SELECT s FROM StudentOrg s " +
            "LEFT JOIN FETCH s.availabilities a " +
            "WHERE (:schoolName IS NULL OR s.schoolName LIKE %:schoolName%) " +
            "AND (:verificationLevel IS NULL OR s.verificationLevel >= :verificationLevel)")
    List<StudentOrg> findMatchingStudentOrgs(
            @Param("schoolName") String schoolName,
            @Param("verificationLevel") Integer verificationLevel
    );

    @Query("""
        SELECT DISTINCT s
        FROM StudentOrg s
        LEFT JOIN s.keywords k
        LEFT JOIN s.availabilities a
        WHERE (:schoolName IS NULL OR s.schoolName LIKE CONCAT('%', :schoolName, '%'))
        AND (:verificationLevel IS NULL OR s.verificationLevel >= :verificationLevel)
        AND (:industry IS NULL 
             OR k.hashtag.category = :industry 
             OR k.hashtag.name = :industry)
        AND (:purpose IS NULL 
             OR k.hashtag.category = :purpose 
             OR k.hashtag.name = :purpose)
        AND (:samplingDate IS NULL
             OR (a.startDate <= :samplingDate AND a.endDate >= :samplingDate))
    """)
    List<StudentOrg> findMatchedOrgsAdvanced(
            @Param("schoolName") String schoolName,
            @Param("verificationLevel") Integer verificationLevel,
            @Param("industry") String industry,
            @Param("purpose") String purpose,
            @Param("samplingDate") LocalDate samplingDate
    );
}