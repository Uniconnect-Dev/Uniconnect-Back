package com.uniConnect.sampling.dto.request;

import jakarta.validation.constraints.*;
import java.util.List;

public record TargetTagRequest(
        @NotEmpty(message = "기본 정보 해시태그는 최소 1개 이상 선택해야 합니다.")
        @Size(max = 5, message = "기본 정보 해시태그는 최대 5개까지 선택할 수 있습니다.")
        List<Long> basicInfoTagIds,

        @NotEmpty(message = "라이프스타일 해시태그는 최소 1개 이상 선택해야 합니다.")
        @Size(max = 5, message = "라이프스타일 해시태그는 최대 5개까지 선택할 수 있습니다.")
        List<Long> lifestyleTagIds
) {}
