package com.uniConnect.campaign.entity;

import com.uniConnect.collaboration.entity.*;
import com.uniConnect.campaign.enums.SenderType;
import com.uniConnect.matching.entity.CollaborationMatchRequest;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "matching_messages")
public class MatchingMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long messageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", length = 20)
    private SenderType senderType;

    @Column(name = "message", columnDefinition = "text")
    private String message;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_request_id")
    private CollaborationMatchRequest matchRequest;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
