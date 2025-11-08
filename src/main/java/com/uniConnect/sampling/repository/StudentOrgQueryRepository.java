package com.uniConnect.sampling.repository;

import com.uniConnect.studentOrg.entity.StudentOrg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

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
}
