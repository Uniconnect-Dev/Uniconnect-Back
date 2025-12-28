package com.uniConnect.curation.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class StudentOrgCurationResponse {
    private Long groupId;
    private String groupName;
    private List<String> hashtags;
    private int memberCount;
    private int avgEventAttendance;
    private double matchingScore;
    private boolean available;
    private boolean safetyApproved;
}