package com.uniConnect.sampling.dto.request;

import com.uniConnect.sampling.enums.IndustryType;
import jakarta.validation.constraints.*;
import java.util.List;

public record IndustryRequest(
        @NotBlank(message = "산업군은 필수 선택 항목입니다.")
        String industry,

        List<Long> industryTagIds,

        @Size(max = 250, message = "세부 요청 사항은 최대 250자까지 입력할 수 있습니다.")
        String detailRequest
) {}
