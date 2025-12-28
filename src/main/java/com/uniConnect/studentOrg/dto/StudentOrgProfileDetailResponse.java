package com.uniConnect.studentOrg.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentOrgProfileDetailResponse {

    private Long studentOrgId;
    private String schoolName;
    private String organizationName;
    private String logoUrl;
    private Integer verificationLevel;
    private Boolean safetyFlag;

    private List<ContactDto> contacts;

    private List<EventDto> events;

    private List<HistoryDto> histories;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContactDto {
        private String name;
        private String phone;
        private String email;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EventDto {
        private Long availabilityId;
        private String eventName;
        private String hostName;
        private String place;
        private String eventType;
        private String description;
        private String targetAgeRange;
        private String targetMajor;
        private String targetInterest;
        private Integer exposureCount;
        private Integer recommendedSampleQty;
        private String promotionProcess;
        private String eventPoints;
        private String promotionPlan;
        private String efficiencyMetric;
        private String startDate;
        private String endDate;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HistoryDto {
        private Long historyId;
        private Long campaignId;
        private String role;
        private String status;
    }
}