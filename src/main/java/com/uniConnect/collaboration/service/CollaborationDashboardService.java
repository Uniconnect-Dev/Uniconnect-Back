package com.uniConnect.collaboration.service;

import com.uniConnect.collaboration.dto.*;
import com.uniConnect.collaboration.entity.*;
import com.uniConnect.collaboration.enums.*;
import com.uniConnect.collaboration.repository.*;
import com.uniConnect.common.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class CollaborationDashboardService {

    private final CollaborationRepository collaborationRepository;
    private final CollaborationTaskRepository taskRepository;
    private final ProductInfoRepository productInfoRepository;
    private final ContentUploadRepository contentUploadRepository;
    private final ReceiptConfirmationRepository receiptRepository;
    private final FileStorageService fileStorageService;

    // 헬퍼: collaboration 조회
    private Collaboration getCollabOrThrow(Long collaborationId) {
        return collaborationRepository.findById(collaborationId)
                .orElseThrow(() -> new IllegalArgumentException("Collaboration not found: " + collaborationId));
    }

    // 1. 기업: 제품 정보 입력
    public ProductInfoResponse addOrUpdateProductInfo(ProductInfoRequest req) {
        Collaboration collab = getCollabOrThrow(req.getCollaborationId());

        ProductInfo info = ProductInfo.builder()
                .collaboration(collab)
                .productName(req.getProductName())
                .quantity(req.getQuantity())
                .description(req.getDescription())
                .providedBy(UploaderType.Company)
                .isShipped(false)
                .build();

        ProductInfo saved = productInfoRepository.save(info);

        upsertTaskStatus(collab, TaskType.ProductInfo, TaskStatus.Done, "Company", null);
        collab.setStatus(CollaborationStatus.InProgress);

        return ProductInfoResponse.from(saved);
    }

    // 2. 기업: 배송 정보 입력 (발송일/운송장/발송여부)
    public ProductInfoResponse updateShippingInfo(ShippingInfoRequest req) {
        Collaboration collab = getCollabOrThrow(req.getCollaborationId());

        // 가장 최근 ProductInfo를 꺼내서 배송정보 채운다고 가정 (실제로는 특정 productInfoId를 지정해도 됨)
        ProductInfo latest = productInfoRepository.findByCollaboration(collab)
                .stream()
                .reduce((a,b) -> b) // 마지막 것
                .orElseThrow(() -> new IllegalStateException("No product info yet"));

        latest.setDeliveryDate(req.getShippingDate());
        latest.setIsShipped(req.getIsShipped());
        latest.setTrackingNo(req.getTrackingNo());
        productInfoRepository.save(latest);

        // 태스크 상태: ShippingInfo → Done if shipped == true, else InProgress
        TaskStatus newStatus = Boolean.TRUE.equals(req.getIsShipped()) ? TaskStatus.Done : TaskStatus.InProgress;
        upsertTaskStatus(collab, TaskType.ShippingInfo, newStatus, "Company", null);

        if (Boolean.TRUE.equals(req.getIsShipped())) {
            collab.setStatus(CollaborationStatus.WaitingReceipt);
        }
        return ProductInfoResponse.from(latest);
    }

    // 3. 기업/학생단체: 이미지 업로드
    public ContentUploadResponse uploadContent(ContentUploadRequest req) {
        Collaboration collab = getCollabOrThrow(req.getCollaborationId());
        String url = null;

        if (req.getImage() != null && !req.getImage().isEmpty()) {
            url = fileStorageService.uploadImage(req.getImage());
        }

        UploaderType uploaderType = switch (req.getUploaderType()) {
            case "Company" -> UploaderType.Company;
            case "Admin" -> UploaderType.Admin;
            default -> UploaderType.StudentOrg;
        };

        ContentUpload upload = ContentUpload.builder()
                .collaboration(collab)
                .imageUrl(url)
                .caption(req.getCaption())
                .uploaderType(uploaderType)
                .build();
        contentUploadRepository.save(upload);

        upsertTaskStatus(collab, TaskType.ContentShare,
                uploaderType == UploaderType.StudentOrg ? TaskStatus.Done : TaskStatus.InProgress,
                uploaderType.name(), null);

        return ContentUploadResponse.from(upload);
    }

    // 4. 학생단체: 인수증 제출
    public ReceiptResponse submitReceipt(ReceiptUploadRequest req) {
        Collaboration collab = getCollabOrThrow(req.getCollaborationId());
        String receiptUrl = fileStorageService.uploadImage(req.getReceiptImage());

        ReceiptConfirmation receipt = ReceiptConfirmation.builder()
                .collaboration(collab)
                .receiverName(req.getReceiverName())
                .location(req.getLocation())
                .receiptImageUrl(receiptUrl)
                .status(ReceiptStatus.WaitingApproval)
                .submittedAt(LocalDateTime.now())
                .build();

        receiptRepository.save(receipt);
        upsertTaskStatus(collab, TaskType.Receipt, TaskStatus.InProgress, "StudentOrg", null);
        collab.setStatus(CollaborationStatus.WaitingReceipt);

        return ReceiptResponse.from(receipt);
    }

    // 5. 기업: 인수증 승인
    public ReceiptResponse approveReceipt(ReceiptApproveRequest req) {
        Collaboration collab = getCollabOrThrow(req.getCollaborationId());
        ReceiptConfirmation latest = receiptRepository.findTopByCollaborationOrderBySubmittedAtDesc(collab)
                .orElseThrow(() -> new IllegalStateException("No receipt uploaded"));

        latest.setStatus(ReceiptStatus.Approved);
        latest.setApprovedAt(LocalDateTime.now());
        receiptRepository.save(latest);

        upsertTaskStatus(collab, TaskType.Receipt, TaskStatus.Done, "Company", null);
        collab.setStatus(CollaborationStatus.Completed);

        return ReceiptResponse.from(latest);
    }

    // 6. 기업: 날짜 픽스
    public TaskResponse fixEventDate(DateFixRequest req) {
        Collaboration collab = getCollabOrThrow(req.getCollaborationId());
        CollaborationTask task = upsertTaskStatus(collab, TaskType.ProductInfo,
                TaskStatus.Pending, "Company", req.getEventDate());
        collab.setStatus(CollaborationStatus.InProgress);
        return TaskResponse.from(task);
    }

    // upsertTaskStatus 리턴 타입 수정
    private CollaborationTask upsertTaskStatus(
            Collaboration collab,
            TaskType type,
            TaskStatus status,
            String updatedBy,
            java.time.LocalDate deadline
    ) {
        CollaborationTask task = taskRepository.findByCollaboration(collab).stream()
                .filter(t -> t.getType() == type)
                .findFirst()
                .orElseGet(() -> CollaborationTask.builder()
                        .collaboration(collab)
                        .type(type)
                        .status(TaskStatus.Pending)
                        .build()
                );

        task.setStatus(status);
        task.setUpdatedBy(updatedBy);
        if (deadline != null) task.setDeadline(deadline);
        return taskRepository.save(task);
    }

    // 진행 현황 통합 조회
    @Transactional(readOnly = true)
    public CollaborationDashboardResponse getDashboard(Long collaborationId) {
        Collaboration collab = getCollabOrThrow(collaborationId);

        var tasks = taskRepository.findByCollaboration(collab);
        var products = productInfoRepository.findByCollaboration(collab);
        var uploads = contentUploadRepository.findByCollaboration(collab);
        var receipts = receiptRepository.findByCollaboration(collab);

        return CollaborationDashboardResponse.from(collab, tasks, products, uploads, receipts);
    }
}
