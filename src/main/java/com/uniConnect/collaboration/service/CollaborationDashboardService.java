package com.uniConnect.collaboration.service;

import com.uniConnect.collaboration.dto.*;
import com.uniConnect.collaboration.entity.*;
import com.uniConnect.collaboration.enums.*;
import com.uniConnect.collaboration.repository.*;
import com.uniConnect.company.entity.Company;
import com.uniConnect.global.exception.CustomException;
import com.uniConnect.global.exception.ErrorCode;
import com.uniConnect.matching.entity.CollaborationMatchRequest;
import com.uniConnect.s3.S3FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import java.util.Comparator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CollaborationDashboardService {

    private final CollaborationRepository collaborationRepository;
    private final CollaborationTaskRepository taskRepository;
    private final ProductInfoRepository productInfoRepository;
    private final ContentUploadRepository contentUploadRepository;
    private final ReceiptConfirmationRepository receiptRepository;
    private final StudentReceiveInfoRepository studentReceiveInfoRepository;
    private final S3FileService s3FileService;

    @Value("${app.s3.bucket}")
    private String bucketName;

    /* ================= 공통 유틸 ================= */

    private Collaboration getCollab(Long id) {
        return collaborationRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));
    }

    private CollaborationMatchRequest getMatchRequest(Collaboration collab) {
        CollaborationMatchRequest match = collab.getMatchRequest();
        if (match == null) {
            throw new CustomException(ErrorCode.ENTITY_NOT_FOUND);
        }
        return match;
    }

    private void validateCompanyOwnership(Collaboration collab, Long userId) {
        Company company = collab.getMatchRequest().getCompany();

        boolean isOwner = company.getUsers().stream()
                .anyMatch(u -> u.getUserId().equals(userId));

        if (!isOwner) {
            throw new AccessDeniedException("기업 소유 계정이 아닙니다.");
        }
    }

    private CollaborationTask upsertTask(
            Collaboration collab,
            TaskType taskType,
            TaskStatus status,
            String updatedBy,
            LocalDate deadline
    ) {
        Optional<CollaborationTask> opt =
                taskRepository.findByCollaboration(collab).stream()
                        .filter(t -> t.getType() == taskType)
                        .findFirst();

        CollaborationTask task = opt.orElseGet(() ->
                CollaborationTask.builder()
                        .collaboration(collab)
                        .type(taskType)
                        .status(TaskStatus.Pending)
                        .build()
        );

        task.setStatus(status);
        task.setUpdatedBy(updatedBy);
        if (deadline != null) task.setDeadline(deadline);

        return taskRepository.save(task);
    }

    /* ================= (1) 수령 정보 ================= */

    public StudentReceiveInfo saveReceiveInfo(StudentReceiveInfoRequest req) {
        Collaboration collab = getCollab(req.getCollaborationId());

        StudentReceiveInfo info = StudentReceiveInfo.builder()
                .collaboration(collab)
                .receiverName(req.getReceiverName())
                .receivePlace(req.getReceivePlace())
                .note(req.getNote())
                .build();

        return studentReceiveInfoRepository.save(info);
    }

    /* ================= (2) 인수증 제출 ================= */

    public ReceiptResponse submitReceipt(
            ReceiptSubmitRequest req,
            MultipartFile receiptImage
    ) throws Exception {

        Collaboration collab = getCollab(req.getCollaborationId());

        String key = "collaboration/" + collab.getId() + "/receipt/" +
                System.currentTimeMillis() + "_" + receiptImage.getOriginalFilename();

        s3FileService.upload(bucketName, key, receiptImage);

        ReceiptConfirmation receipt = ReceiptConfirmation.builder()
                .collaboration(collab)
                .receiptImageUrl("https://" + bucketName + ".s3.amazonaws.com/" + key)
                .receiverName(req.getReceiverName())
                .location(req.getLocation())
                .receivedQuantity(req.getReceivedQuantity())
                .hasDefect(req.getHasDefect())
                .expirationDate(
                        req.getExpirationDate() != null
                                ? LocalDate.parse(req.getExpirationDate())
                                : null
                )
                .receivedAt(LocalDateTime.now())
                .submittedAt(LocalDateTime.now())
                .signatureImageBase64(req.getSignatureImage())
                .signatureTimestamp(req.getTimestamp())
                .status(ReceiptStatus.WaitingApproval)
                .build();

        receiptRepository.save(receipt);

        collab.setStatus(CollaborationStatus.WaitingReceiptApproval);

        upsertTask(collab, TaskType.Receipt, TaskStatus.InProgress, "StudentOrg", null);

        return ReceiptResponse.from(receipt);
    }

    /* ================= (3) 제품 정보 ================= */

    public ProductInfoResponse addOrUpdateProductInfo(ProductInfoRequest req, Long userId) {
        Collaboration collab = getCollab(req.getCollaborationId());
        validateCompanyOwnership(collab, userId);

        List<ProductInfo> infos = productInfoRepository.findByCollaboration(collab);
        ProductInfo info = infos.isEmpty()
                ? ProductInfo.builder().collaboration(collab).build()
                : infos.get(0);

        info.setProductName(req.getProductName());
        info.setQuantity(req.getQuantity());

        productInfoRepository.save(info);

        upsertTask(collab, TaskType.ProductInfo, TaskStatus.Done, "Company", null);

        return ProductInfoResponse.from(info);
    }

    /* ================= (4) 콘텐츠 업로드 ================= */

    public ContentUploadResponse uploadContentToS3(
            Long collaborationId,
            String uploaderType,
            String caption,
            MultipartFile image
    ) throws Exception {

        Collaboration collab = getCollab(collaborationId);

        String key = "collaboration/" + collab.getId() + "/content/" +
                System.currentTimeMillis() + "_" + image.getOriginalFilename();

        s3FileService.upload(bucketName, key, image);

        UploaderType uploader = UploaderType.valueOf(uploaderType);

        ContentUpload upload = ContentUpload.builder()
                .collaboration(collab)
                .uploaderType(uploader)
                .imageUrl("https://" + bucketName + ".s3.amazonaws.com/" + key)
                .caption(caption)
                .uploadedAt(LocalDateTime.now())
                .build();

        contentUploadRepository.save(upload);

        return ContentUploadResponse.from(upload);
    }

    /* ================= (5) 인수증 승인 ================= */

    @Transactional
    public ReceiptResponse approveReceiptByAdmin(
            Long collaborationId,
            String role   // 컨트롤러에서 JWT role 전달
    ) {
        if (!"Admin".equals(role)) {
            throw new AccessDeniedException("어드민 권한이 필요합니다.");
        }

        Collaboration collab = collaborationRepository.findById(collaborationId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        ReceiptConfirmation receipt = receiptRepository
                .findTopByCollaborationOrderBySubmittedAtDesc(collab)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        if (receipt.getStatus() != ReceiptStatus.WaitingApproval) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        receipt.setStatus(ReceiptStatus.Approved);
        receipt.setApprovedAt(LocalDateTime.now());

        receiptRepository.save(receipt);

        collab.setStatus(CollaborationStatus.WaitingReportUpload);

        upsertTask(
                collab,
                TaskType.Receipt,
                TaskStatus.Done,
                "Admin",
                null
        );

        return ReceiptResponse.from(receipt);
    }

    public ReceiptResponse approveReceipt(Long collaborationId, Long userId) {
        Collaboration collab = getCollab(collaborationId);
        validateCompanyOwnership(collab, userId);

        List<ReceiptConfirmation> receipts =
                receiptRepository.findByCollaboration(collab);

        if (receipts.isEmpty()) {
            throw new CustomException(ErrorCode.ENTITY_NOT_FOUND);
        }

        ReceiptConfirmation receipt = receipts.get(0);
        receipt.setStatus(ReceiptStatus.Approved);
        receipt.setApprovedAt(LocalDateTime.now());
        collab.setStatus(CollaborationStatus.WaitingReportUpload);

        receiptRepository.save(receipt);

        upsertTask(collab, TaskType.Receipt, TaskStatus.Done, "Company", null);

        return ReceiptResponse.from(receipt);
    }
    // (5) 기업 - 배송 정보 입력
    public ProductInfoResponse updateShippingInfo(
            ShippingInfoRequest req,
            Long userId
    ) {
        Collaboration collab = getCollab(req.getCollaborationId());
        validateCompanyOwnership(collab, userId);

        ProductInfo latest = productInfoRepository
                .findTopByCollaborationOrderByCreatedAtDesc(collab)
                .orElseThrow(() -> new IllegalStateException("제품 정보가 없습니다."));

        latest.setDeliveryDate(req.getShippingDate());
        latest.setTrackingNo(req.getTrackingNo());
        latest.setIsShipped(req.getIsShipped());

        productInfoRepository.save(latest);

        upsertTask(
                collab,
                TaskType.ShippingInfo,
                req.getIsShipped() ? TaskStatus.Done : TaskStatus.InProgress,
                "Company",
                null
        );

        return ProductInfoResponse.from(latest);
    }

    // (7) 기업 - 행사 날짜 픽스
    public TaskResponse fixEventDate(
            DateFixRequest req,
            Long userId
    ) {
        Collaboration collab = getCollab(req.getCollaborationId());
        validateCompanyOwnership(collab, userId);

        CollaborationTask task = upsertTask(
                collab,
                TaskType.EventDateFix,
                TaskStatus.Pending,
                "Company",
                req.getEventDate()
        );

        return TaskResponse.from(task);
    }


    /* ================= (6) 대시보드 조회 ================= */

    @Transactional(readOnly = true)
    public CollaborationDashboardResponse getDashboard(
            Long collaborationId,
            Long userId,
            String role
    ) {

        Collaboration collab = getCollab(collaborationId);
        CollaborationMatchRequest match = collab.getMatchRequest();

        boolean hasAccess = switch (role) {
            case "StudentOrg" ->
                    match.getStudentOrg().getUsers().stream()
                            .anyMatch(u -> u.getUserId().equals(userId));
            case "Company" ->
                    match.getCompany().getUsers().stream()
                            .anyMatch(u -> u.getUserId().equals(userId));
            case "Admin" -> true;
            default -> false;
        };

        if (!hasAccess) throw new CustomException(ErrorCode.FORBIDDEN);

        return CollaborationDashboardResponse.from(
                collab,
                taskRepository.findByCollaboration(collab),
                productInfoRepository.findByCollaboration(collab),
                contentUploadRepository.findByCollaboration(collab),
                receiptRepository.findByCollaboration(collab),
                role
        );
    }
}