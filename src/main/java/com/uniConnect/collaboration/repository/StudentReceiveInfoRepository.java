package com.uniConnect.collaboration.repository;

import com.uniConnect.collaboration.entity.Collaboration;
import com.uniConnect.collaboration.entity.StudentReceiveInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentReceiveInfoRepository extends JpaRepository<StudentReceiveInfo, Long> {
    Optional<StudentReceiveInfo> findByCollaboration(Collaboration collaboration);
}