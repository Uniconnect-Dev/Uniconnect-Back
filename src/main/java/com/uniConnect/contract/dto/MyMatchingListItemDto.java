package com.uniConnect.contract.dto;

import com.uniConnect.contract.enums.ContractStatus;
import com.uniConnect.studentOrg.enums.CollaborationType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class MyMatchingListItemDto {

    private Long matchRequestId;
    private LocalDateTime matchedAt;
    private String studentOrgName;
    private CollaborationType collaborationType;
    private String contractStatus; // "서명 전" / "계약 완료"

    public static MyMatchingListItemDto of(
            Long matchRequestId,
            LocalDateTime matchedAt,
            String studentOrgName,
            CollaborationType collaborationType,
            ContractStatus contractStatus
    ) {
        return MyMatchingListItemDto.builder()
                .matchRequestId(matchRequestId)
                .matchedAt(matchedAt)
                .studentOrgName(studentOrgName)
                .collaborationType(collaborationType)
                .contractStatus(
                        contractStatus == ContractStatus.Signed
                                ? "계약 완료"
                                : "서명 전"
                )
                .build();
    }
}
