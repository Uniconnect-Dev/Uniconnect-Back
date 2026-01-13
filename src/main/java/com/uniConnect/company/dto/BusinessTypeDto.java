
package com.uniConnect.company.dto;

import com.uniConnect.company.entity.BusinessType;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessTypeDto {

    private Long businessTypeId;
    private String name;
    private String description;

    public static BusinessTypeDto of(BusinessType businessType) {
        return BusinessTypeDto.builder()
                .businessTypeId(businessType.getBusinessTypeId())
                .name(businessType.getName())
                .description(businessType.getDescription())
                .build();
    }

    public static BusinessTypeDto of(Long id, String name) {
        return BusinessTypeDto.builder()
                .businessTypeId(id)
                .name(name)
                .build();
    }
}