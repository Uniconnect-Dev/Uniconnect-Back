package com.uniConnect.collaboration.service;

import com.uniConnect.sampling.entity.SamplingRequest;
import com.uniConnect.sampling.repository.SamplingRequestRepository;
import com.uniConnect.collaboration.dto.*;
import com.uniConnect.collaboration.entity.*;
import com.uniConnect.collaboration.enums.*;
import com.uniConnect.collaboration.repository.*;
import com.uniConnect.common.service.FileStorageService;
import com.uniConnect.signature.dto.SignatureRequest;
import com.uniConnect.campaign.repository.MatchingRequestRepository;
import com.uniConnect.member.enums.UserRole;
import com.uniConnect.company.entity.Company;
import com.uniConnect.global.exception.ErrorCode;
import com.uniConnect.global.exception.CustomException;
import com.uniConnect.signature.service.SignatureService;
import com.uniConnect.s3.S3FileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private final S3FileService s3FileService;
    private final ShippingInfoRepository shippingInfoRepository;
    private final StudentReceiveInfoRepository studentReceiveInfoRepository;
    private final MatchingRequestRepository matchingRequestRepository;
    private final SignatureService signatureService;
    private final SamplingRequestRepository samplingRequestRepository;

    @Value("${app.s3.bucket}")
    private String bucketName;


    /* ==========================================================
                     공통 유틸 / 검증 함수
    ========================================================== */

    private Collaboration getCollab(Long id) {
        return collaborationRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));
    }

    public Long getOwnerId(Long requestId) {
        SamplingRequest req = samplingRequestRepository.findById(requestId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        return req.getUser().getUserId();
    }

    private LocalDate safeParse(String value) {
        if (value == null || value.isBlank()) return null;
        return LocalDate.parse(value);
    }

    private void validateCompanyOwnership(Collaboration collab, Long userId) {

        var campaign = collab.getMatching().getCampaign();

        if (campaign == null || campaign.getCompany() == null) {
            throw new AccessDeniedException("기업 정보 없음");
        }

        boolean isOwner = campaign.getCompany().getUsers().stream()
                .anyMatch(u -> u.getUserId().equals(userId));

        if (!isOwner) {
            throw new AccessDeniedException("기업 소유 계정 아님");
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


    /* ==========================================================
                   (1) 학생단체 수령 정보 저장
    ========================================================== */
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


    /* ==========================================================
                    (2) 학생단체 인수증 제출
    ========================================================== */
    public ReceiptResponse submitReceipt(
            ReceiptSubmitRequest req,
            MultipartFile receiptImage,
            Long userId
    ) throws Exception {

        Collaboration collab = getCollab(req.getCollaborationId());

        String filename = receiptImage.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            filename = "receipt_" + System.currentTimeMillis() + ".png";
        }

        String key = "collaboration/" + collab.getId() + "/receipt/" + filename;

        s3FileService.upload(bucketName, key, receiptImage);

        String receiptUrl = "https://" + bucketName + ".s3.amazonaws.com/" + key;


        ReceiptConfirmation receipt = ReceiptConfirmation.builder()
                .collaboration(collab)
                .receiptImageUrl(receiptUrl)
                .receiverName(req.getReceiverName())
                .location(req.getLocation())
                .receivedQuantity(req.getReceivedQuantity())
                .hasDefect(req.getHasDefect())
                .expirationDate(
                        req.getExpirationDate() != null ? LocalDate.parse(req.getExpirationDate()) : null
                )
                .receivedAt(LocalDateTime.now())
                .submittedAt(LocalDateTime.now())
                .status(ReceiptStatus.WaitingApproval)
                .signatureImageBase64(req.getSignatureImage())
                .signatureTimestamp(req.getTimestamp())
                .build();

        receiptRepository.save(receipt);


        /* ===== 3. Collaboration 상태 변경 ===== */
        collab.setStatus(CollaborationStatus.WaitingReportUpload);

        upsertTask(
                collab,
                TaskType.Receipt,
                TaskStatus.InProgress,
                "StudentOrg",
                null
        );

        return ReceiptResponse.from(receipt);
    }


    /* ==========================================================
                    (3) 기업 인수증 승인
    ========================================================== */
    public ReceiptResponse approveReceipt(Long collaborationId, Long userId) {

        Collaboration collab = getCollab(collaborationId);
        validateCompanyOwnership(collab, userId);

        ReceiptConfirmation latest = receiptRepository
                .findTopByCollaborationOrderBySubmittedAtDesc(collab)
                .orElseThrow(() -> new IllegalStateException("업로드된 인수증이 없습니다."));

        latest.setStatus(ReceiptStatus.Approved);
        latest.setApprovedAt(LocalDateTime.now());
        receiptRepository.save(latest);

        collab.setStatus(CollaborationStatus.Completed);

        upsertTask(
                collab,
                TaskType.Receipt,
                TaskStatus.Done,
                "Company",
                null
        );

        return ReceiptResponse.from(latest);
    }


    /* ==========================================================
                 (4) 기업 - 제품 정보 등록
    ========================================================== */
    public ProductInfoResponse addOrUpdateProductInfo(ProductInfoRequest req, Long userId) {

        Collaboration collab = getCollab(req.getCollaborationId());
        validateCompanyOwnership(collab, userId);

        ProductInfo info = ProductInfo.builder()
                .collaboration(collab)
                .productName(req.getProductName())
                .quantity(req.getQuantity())
                .description(req.getDescription())
                .providedBy(UploaderType.Company)
                .isShipped(false)
                .build();

        productInfoRepository.save(info);

        upsertTask(collab, TaskType.ProductInfo, TaskStatus.Done, "Company", null);

        return ProductInfoResponse.from(info);
    }


    /* ==========================================================
                 (5) 기업 - 배송 정보 입력
    ========================================================== */
    public ProductInfoResponse updateShippingInfo(ShippingInfoRequest req, Long userId) {

        Collaboration collab = getCollab(req.getCollaborationId());
        validateCompanyOwnership(collab, userId);

        ProductInfo latest = productInfoRepository
                .findTopByCollaborationOrderByCreatedAtDesc(collab)
                .orElseThrow(() -> new IllegalStateException("제품 정보가 없습니다."));

        latest.setDeliveryDate(req.getShippingDate());
        latest.setTrackingNo(req.getTrackingNo());
        latest.setIsShipped(req.getIsShipped());

        productInfoRepository.save(latest);

        TaskStatus newStatus = req.getIsShipped() ? TaskStatus.Done : TaskStatus.InProgress;

        upsertTask(collab, TaskType.ShippingInfo, newStatus, "Company", null);

        return ProductInfoResponse.from(latest);
    }


    /* ==========================================================
         (6) 기업/학생단체 - S3 이미지 업로드
    ========================================================== */
    public ContentUploadResponse uploadContentToS3(
            Long collaborationId,
            String uploaderTypeStr,
            String caption,
            MultipartFile image
    ) throws Exception {

        Collaboration collab = getCollab(collaborationId);

        String filename = image.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            filename = "upload_" + System.currentTimeMillis() + ".png";
        }

        String key = "collaboration/" + collaborationId + "/uploads/" + filename;

        s3FileService.upload(bucketName, key, image);
        String url = "https://" + bucketName + ".s3.amazonaws.com/" + key;

        UploaderType uploader = switch (uploaderTypeStr.toLowerCase()) {
            case "company" -> UploaderType.Company;
            case "admin" -> UploaderType.Admin;
            default -> UploaderType.StudentOrg;
        };

        ContentUpload upload = ContentUpload.builder()
                .collaboration(collab)
                .imageUrl(url)
                .caption(caption)
                .uploaderType(uploader)
                .build();

        contentUploadRepository.save(upload);

        TaskStatus status =
                (uploader == UploaderType.StudentOrg)
                        ? TaskStatus.Done
                        : TaskStatus.InProgress;

        upsertTask(collab, TaskType.ContentShare, status, uploader.name(), null);

        return ContentUploadResponse.from(upload);
    }


    /* ==========================================================
                  (7) 기업 - 행사 날짜 픽스
    ========================================================== */
    public TaskResponse fixEventDate(DateFixRequest req, Long userId) {

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


    /* ==========================================================
                (8) 협업 대시보드 조회
    ========================================================== */
    @Transactional(readOnly = true)
    public CollaborationDashboardResponse getDashboard(
            Long collaborationId,
            Long userId,
            String role
    ) {

        Collaboration collab = getCollab(collaborationId);
        Long matchingId = collab.getMatching().getMatchingId();

        boolean hasAccess = switch (role) {
            case "StudentOrg" ->
                    matchingRequestRepository.existsByMatchingIdAndStudentOrgUsers(matchingId, userId);
            case "Company" ->
                    matchingRequestRepository.existsByMatchingIdAndCompanyUsers(matchingId, userId);
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