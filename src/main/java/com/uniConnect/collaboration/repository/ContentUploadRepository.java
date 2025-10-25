package com.uniConnect.collaboration.repository;

import com.uniConnect.collaboration.entity.ContentUpload;
import com.uniConnect.collaboration.entity.Collaboration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContentUploadRepository extends JpaRepository<ContentUpload, Long> {
    List<ContentUpload> findByCollaboration(Collaboration collaboration);
}
