package com.uniConnect.sampling.dto.response;

import com.uniConnect.sampling.enums.IndustryType;
import com.uniConnect.sampling.enums.SamplingStatus;
import java.time.LocalDate;
import java.util.List;

public record SamplingRequestSummaryResponse(
        Long id,
        String schoolName,
        String orgName,
        String contactName,
        String phone,
        String email,
        String eventTitle,
        String eventDescription,
        Integer requestedQuantity,
        LocalDate eventStartDate,
        LocalDate eventEndDate,
        IndustryType industry,
        String proposalFileUrl,
        SamplingStatus status,
        List<String> selectedTags
) {}
