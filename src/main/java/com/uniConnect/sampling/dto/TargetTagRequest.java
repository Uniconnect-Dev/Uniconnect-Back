package com.uniConnect.sampling.dto.request;

import jakarta.validation.constraints.*;
import java.util.List;

public record TargetTagRequest(
        @NotNull @Size(min = 1, max = 5) List<Long> basicInfoTagIds,
        @NotNull @Size(min = 1, max = 5) List<Long> lifestyleTagIds
) {}
