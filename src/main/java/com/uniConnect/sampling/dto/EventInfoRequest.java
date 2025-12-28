package com.uniConnect.sampling.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

public record EventInfoRequest(
    @NotBlank String eventTitle,
    @NotBlank @Size(max = 500) String eventDescription,
    @NotNull Integer requestedQuantity,
    @NotNull LocalDate eventStartDate,
    @NotNull LocalDate eventEndDate,
    @NotNull @Size(min = 1) List<Long> eventTagIds
) {}
