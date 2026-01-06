package com.uniConnect.matching.dto;

import com.uniConnect.studentOrg.enums.CollaborationType;
import java.util.List;

public record CompanyMatchRequest(
        CollaborationType collaborationType,
        List<Long> targetIds
) {}