package com.uniConnect.collaboration.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentReceiveInfoRequest {

    private Long collaborationId;

    private String receiverName;
    private String receivePlace;
    private String note;
}
