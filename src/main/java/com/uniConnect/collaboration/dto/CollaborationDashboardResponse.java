package com.uniConnect.collaboration.dto;

import com.uniConnect.collaboration.entity.*;
import com.uniConnect.collaboration.enums.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollaborationDashboardResponse {

    private Long collaborationId;
    private CollaborationStatus collaborationStatus;
    private String userRole;

    private List<TaskInfo> tasks;
    private List<ProductInfoBlock> products;
    private List<ContentUploadBlock> uploads;
    private ReceiptBlock latestReceipt;
    private CompanySectionDto companySection;
    private StudentSectionDto studentSection;

    @Data
    @Builder
    public static class TaskInfo {
        private TaskType type;
        private TaskStatus status;
        private LocalDate deadline;
        private String updatedBy;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    public static class ProductInfoBlock {
        private String productName;
        private Integer quantity;
        private String description;
        private String imageUrl;
        private String logoUrl;
        private LocalDate deliveryDate;
        private Boolean isShipped;
        private String trackingNo;
    }

    @Data
    @Builder
    public static class ContentUploadBlock {
        private String imageUrl;
        private String caption;
        private String uploaderType;
        private LocalDateTime uploadedAt;
    }

    @Data
    @Builder
    public static class ReceiptBlock {
        private String receiverName;
        private String location;
        private String receiptImageUrl;
        private ReceiptStatus status;
        private LocalDateTime submittedAt;
        private LocalDateTime approvedAt;
    }

    public static CollaborationDashboardResponse from(
            Collaboration collab,
            List<CollaborationTask> tasks,
            List<ProductInfo> products,
            List<ContentUpload> uploads,
            List<ReceiptConfirmation> receipts,
            String role
    ) {
        ReceiptConfirmation latest = receipts.stream()
                .reduce((a, b) -> b)
                .orElse(null);

        // 빌더 생성
        CollaborationDashboardResponse.CollaborationDashboardResponseBuilder builder =
                CollaborationDashboardResponse.builder()
                        .collaborationId(collab.getId())
                        .collaborationStatus(collab.getStatus())
                        .userRole(role)
                        .tasks(tasks.stream()
                                .map(t -> TaskInfo.builder()
                                        .type(t.getType())
                                        .status(t.getStatus())
                                        .deadline(t.getDeadline())
                                        .updatedBy(t.getUpdatedBy())
                                        .updatedAt(t.getUpdatedAt())
                                        .build())
                                .toList())
                        .products(products.stream()
                                .map(p -> ProductInfoBlock.builder()
                                        .productName(p.getProductName())
                                        .quantity(p.getQuantity())
                                        .description(p.getDescription())
                                        .imageUrl(p.getImageUrl())
                                        .logoUrl(p.getLogoUrl())
                                        .deliveryDate(p.getDeliveryDate())
                                        .isShipped(p.getIsShipped())
                                        .trackingNo(p.getTrackingNo())
                                        .build())
                                .toList())
                        .uploads(uploads.stream()
                                .map(u -> ContentUploadBlock.builder()
                                        .imageUrl(u.getImageUrl())
                                        .caption(u.getCaption())
                                        .uploaderType(u.getUploaderType().name())
                                        .uploadedAt(u.getUploadedAt())
                                        .build())
                                .toList());

        // 인수증이 있을 경우 세팅
        if (latest != null) {
            builder.latestReceipt(
                    ReceiptBlock.builder()
                            .receiverName(latest.getReceiverName())
                            .location(latest.getLocation())
                            .receiptImageUrl(latest.getReceiptImageUrl())
                            .status(latest.getStatus())
                            .submittedAt(latest.getSubmittedAt())
                            .approvedAt(latest.getApprovedAt())
                            .build()
            );
        }

        CollaborationDashboardResponse response = builder.build();

        // 역할별 필드 조정
        if ("Company".equalsIgnoreCase(role)) {
            response.setLatestReceipt(null);
        } else if ("StudentOrg".equalsIgnoreCase(role)) {
            response.getProducts().forEach(p -> p.setTrackingNo(null));
        }

        return response;
    }
}