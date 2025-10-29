package com.uniConnect.collaboration.dto;

import com.uniConnect.collaboration.entity.CollaborationTask;
import com.uniConnect.collaboration.enums.TaskStatus;
import com.uniConnect.collaboration.enums.TaskType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class TaskResponse {
    private Long taskId;
    private TaskType type;
    private TaskStatus status;
    private LocalDate deadline;
    private String updatedBy;
    private LocalDateTime updatedAt;

    public static TaskResponse from(CollaborationTask task) {
        return TaskResponse.builder()
                .taskId(task.getTaskId())
                .type(task.getType())
                .status(task.getStatus())
                .deadline(task.getDeadline())
                .updatedBy(task.getUpdatedBy())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
