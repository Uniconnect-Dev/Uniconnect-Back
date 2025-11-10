package com.uniConnect.collaboration.dto;

import com.uniConnect.collaboration.entity.ReceiptConfirmation;
import com.uniConnect.collaboration.enums.ReceiptStatus;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReceiptSectionDto {

    private String status;
    private String message;

    public static ReceiptSectionDto from(List<ReceiptConfirmation> receipts) {
        if (receipts == null || receipts.isEmpty()) {
            return new ReceiptSectionDto(
                    ReceiptStatus.PendingUpload.name(),
                    "기업으로부터 제품을 수령하셨다면, 인수증을 업로드해주세요."
            );
        }

        ReceiptConfirmation receipt = receipts.get(0);

        String msg = switch (receipt.getStatus()) {
            case PendingUpload -> "기업으로부터 제품을 수령하셨다면, 인수증을 업로드해주세요.";
            case WaitingApproval -> "인수증 검토 중입니다. 관리자 승인 후 기업에 전달됩니다.";
            case Approved -> "인수증이 승인되었습니다.";
            default -> "인수증 상태를 확인할 수 없습니다.";
        };

        return new ReceiptSectionDto(receipt.getStatus().name(), msg);
    }
}
