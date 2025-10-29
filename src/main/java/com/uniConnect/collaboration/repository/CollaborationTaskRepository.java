package com.uniConnect.collaboration.repository;

import com.uniConnect.collaboration.entity.CollaborationTask;
import com.uniConnect.collaboration.entity.Collaboration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CollaborationTaskRepository extends JpaRepository<CollaborationTask, Long> {
    List<CollaborationTask> findByCollaboration(Collaboration collaboration);
}
