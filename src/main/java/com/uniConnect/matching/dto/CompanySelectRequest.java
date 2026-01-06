package com.uniConnect.matching.dto;

import java.util.List;

public record CompanySelectRequest(
        List<Long> companyIds
) {}
