package com.uniConnect.studentOrg.repository;

import com.uniConnect.studentOrg.entity.StudentOrgKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentOrgKeywordRepository extends JpaRepository<StudentOrgKeyword, Long> {

    @Query("""
        SELECT sk.hashtag.hashtagId
        FROM StudentOrgKeyword sk
        WHERE sk.studentOrg.studentOrgId = :studentOrgId
    """)
    List<Long> findHashtagIdsByStudentOrg(@Param("studentOrgId") Long studentOrgId);
}