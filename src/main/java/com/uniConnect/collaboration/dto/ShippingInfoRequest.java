package com.uniConnect.collaboration.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ShippingInfoRequest {
    private Long collaborationId;
    private LocalDate shippingDate;
    private Boolean isShipped;
    private String trackingNo;
}
