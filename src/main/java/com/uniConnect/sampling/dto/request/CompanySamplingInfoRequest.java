package com.uniConnect.sampling.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record CompanySamplingInfoRequest(

        @NotNull(message = "산업군 ID는 필수입니다.")
        Long industryId,

        @NotBlank(message = "샘플링 목적은 필수입니다.")
        @Size(min = 10, max = 300, message = "샘플링 목적은 10~300자여야 합니다.")
        String samplingPurpose,

        @NotNull(message = "샘플링 시작일은 필수입니다.")
        LocalDate samplingStartDate,

        @NotNull(message = "샘플링 종료일은 필수입니다.")
        LocalDate samplingEndDate,

        @NotBlank(message = "제품/서비스명은 필수입니다.")
        @Pattern(
                regexp = "^[가-힣a-zA-Z0-9\\-+/& ]{2,100}$",
                message = "제품명은 2~100자이며 한글/영문/숫자/-,&,/,+ 만 허용됩니다."
        )
        String productName,

        @NotNull(message = "제품 개수는 필수입니다.")
        @Min(value = 1, message = "제품 개수는 최소 1개 이상이어야 합니다.")
        @Max(value = 1_000_000, message = "제품 개수는 최대 1,000,000개까지 입력 가능합니다.")
        Integer productCount

) {}
