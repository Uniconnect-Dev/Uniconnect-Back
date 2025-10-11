package com.uniConnect.collaboration.entity;

import com.uniConnect.campaign.entity.MatchingRequest;
import com.uniConnect.collaboration.enums.TaskStatus;
import com.uniConnect.collaboration.enums.TaskType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "collaboration_tasks")
public class CollaborationTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Long taskId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 30)
    private TaskType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private TaskStatus status;

    @Column(name = "deadline")
    private LocalDate deadline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matching_id")
    private MatchingRequest matching;
}
