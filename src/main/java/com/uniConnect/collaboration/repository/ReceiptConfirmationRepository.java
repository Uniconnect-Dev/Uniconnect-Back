package com.uniConnect.collaboration.repository;

import com.uniConnect.collaboration.entity.ReceiptConfirmation;
import com.uniConnect.collaboration.entity.Collaboration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReceiptConfirmationRepository extends JpaRepository<ReceiptConfirmation, Long> {
    List<ReceiptConfirmation> findByCollaboration(Collaboration collaboration);

    Optional<ReceiptConfirmation> findTopByCollaborationOrderBySubmittedAtDesc(Collaboration collaboration);
}
