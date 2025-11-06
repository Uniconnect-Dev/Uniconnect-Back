package com.uniConnect.studentOrg.repository;

import com.uniConnect.studentOrg.entity.StudentOrg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StudentOrgRepository extends JpaRepository<StudentOrg, Long> {
    Optional<StudentOrg> findByUser_UserId(Long userId);
    boolean existsByUser_UserId(Long userId);
}
