package com.uniConnect.matching.entity;

import com.uniConnect.campaign.enums.MatchingStatus;
import com.uniConnect.sampling.enums.IndustryType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "collaboration_match_requests")
public class CollaborationMatchRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentOrgId;
    private Long companyId;

    private String eventTitle;

    private LocalDate desiredDate;

    @Enumerated(EnumType.STRING)
    private IndustryType industry;
    private String collaborationType;

    @Enumerated(EnumType.STRING)
    private MatchingStatus status;

    private LocalDateTime requestedAt;
    private LocalDateTime respondedAt;

}