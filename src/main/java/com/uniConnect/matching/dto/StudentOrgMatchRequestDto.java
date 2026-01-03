package com.uniConnect.matching.dto;

import java.util.List;

public record StudentOrgMatchRequestDto(
        Long campaignId,
        List<Long> companyIds
) {}