package com.uniConnect.studentOrg.repository;

import com.uniConnect.studentOrg.entity.StudentOrgHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentOrgHistoryRepository extends JpaRepository<StudentOrgHistory, Long> {

    List<StudentOrgHistory> findByStudentOrg_StudentOrgId(Long studentOrgId);
}