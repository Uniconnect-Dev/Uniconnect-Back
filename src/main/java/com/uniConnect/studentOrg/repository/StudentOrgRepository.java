package com.uniConnect.studentOrg.repository;

import com.uniConnect.studentOrg.entity.StudentOrg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentOrgRepository extends JpaRepository<StudentOrg, Long> {
}
