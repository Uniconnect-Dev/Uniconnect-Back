package com.uniConnect.collaboration.dto;

import lombok.Data;

@Data
public class ReceiveInfoRequest {
    private Long collaborationId;
    private String receiverName;
    private String location;
}
