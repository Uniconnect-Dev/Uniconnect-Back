package com.uniConnect.collaboration.dto;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ReceiptSubmitRequest {

    @Schema(example = "3", description = "협업 ID")
    private Long collaborationId;

    @Schema(example = "홍길동", description = "수령자 이름")
    private String receiverName;

    @Schema(example = "이화여대 ECC 1층", description = "수령 장소")
    private String location;

    @Schema(example = "100", description = "수령 수량")
    private Integer receivedQuantity;

    @Schema(example = "false", description = "하자 여부")
    private Boolean hasDefect;

    @Schema(example = "2025-01-10", description = "유통기한")
    private String expirationDate;

    @Schema(example = "data:image/png;base64,....", description = "서명 base64 이미지")
    private String signatureImage;

    @Schema(example = "1735900000000", description = "서명 timestamp(밀리초)")
    private Long timestamp;
}