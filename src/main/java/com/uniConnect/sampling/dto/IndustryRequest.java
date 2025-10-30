package com.uniConnect.sampling.dto.request;

import com.uniConnect.sampling.enums.IndustryType;
import jakarta.validation.constraints.*;
import java.util.List;

public record IndustryRequest(
    @NotNull IndustryType industry,
    @NotNull @Size(min = 1) List<Long> industryTagIds
) {}
