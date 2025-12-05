package com.uniConnect.sampling.dto.request;

import java.util.List;

public record CompanySelectRequest(
        List<Long> companyIds
) {}
