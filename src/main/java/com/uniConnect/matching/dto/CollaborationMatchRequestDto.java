package com.uniConnect.matching.dto;

import com.uniConnect.sampling.enums.IndustryType;
import com.uniConnect.studentOrg.enums.CollaborationType;
import lombok.Data;

@Data
public class CollaborationMatchRequestDto {

    private Long studentOrgId;
    private Long companyId;

    private String eventTitle;
    private String desiredDate;

    private String industry;
    private String collaborationType;
}
